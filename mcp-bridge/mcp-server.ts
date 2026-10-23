import { McpServer } from '@modelcontextprotocol/server';
import { serveStdio } from '@modelcontextprotocol/server/stdio';
import * as z from 'zod/v4';

const BASE_URL = process.env.CIJHIPSTER_API_URL ?? 'http://localhost:8088';

type Operation = {
  summary?: string;
  description?: string;
  operationId?: string;
  tags?: string[];
  parameters?: unknown[];
};

type ApiSpec = {
  openapi?: string;
  paths?: Record<string, Record<string, Operation>>;
};

//
function allowed(path: string): boolean {
  if (!path.startsWith('/api/')) return false;

  const blocked = [
    '/api/admin',
    '/api/account',
    '/api/authenticate',
    '/api/register',
    '/api/activate',
    '/api/import',
    '/api/users',
    '/api/management',
  ];

  return !blocked.some(prefix => path === prefix || path.startsWith(prefix + '/'));
}

async function loadSpec(): Promise<ApiSpec> {
  const token = process.env.CIJHIPSTER_TOKEN;

  if (!token) {
    throw new Error('Не настроен CIJHIPSTER_TOKEN для доступа к OpenAPI');
  }

  const response = await fetch(new URL('/v3/api-docs', BASE_URL), {
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: 'application/json',
    },
    signal: AbortSignal.timeout(10000),
    redirect: 'error',
  });

  if (!response.ok) {
    throw new Error(`OpenAPI HTTP ${response.status}`);
  }

  const contentType = response.headers.get('content-type') ?? '';

  if (!contentType.includes('json')) {
    throw new Error(`Ожидался OpenAPI JSON, получен ${contentType}`);
  }

  const spec = (await response.json()) as ApiSpec;

  if (!spec.openapi || !spec.paths) {
    throw new Error('Backend не вернул корректную OpenAPI-документацию');
  }

  return spec;
}

function success(data: unknown) {
  return {
    content: [{ type: 'text' as const, text: JSON.stringify(data, null, 2) }],
  };
}

function failure(error: unknown) {
  return {
    isError: true,
    content: [{
      type: 'text' as const,
      text: error instanceof Error ? error.message : 'Неизвестная ошибка',
    }],
  };
}

serveStdio(() => {
  const server = new McpServer({
    name: 'cijhipster-mcp',
    version: '2.0.0',
  });

  // Инструмент №1: поиск методов API по описанию.
  server.registerTool(
    'explore_api',
    {
      description:
        'Изучить REST API приложения CIjhipster. ' +
        'Искать методы, параметры и описание функций backend. ' +
        'Используй перед вызовом call_api.',
      inputSchema: z.object({
        search: z.string().default(''),
      }),
    },
    async ({ search }) => {
      try {
        const spec = await loadSpec();
        const query = search.toLowerCase().trim();
        const endpoints: unknown[] = [];

        for (const [path, methods] of Object.entries(spec.paths ?? {})) {
          if (!allowed(path)) continue;

          const operation = methods.get;
          if (!operation) continue;

          const item = {
            path,
            method: 'GET',
            summary: operation.summary ?? '',
            description: operation.description ?? '',
            operationId: operation.operationId ?? '',
            tags: operation.tags ?? [],
            parameters: operation.parameters ?? [],
          };

          if (query && !JSON.stringify(item).toLowerCase().includes(query)) {
            continue;
          }

          endpoints.push(item);
        }

        return success({
          totalMatches: endpoints.length,
          endpoints: endpoints.slice(0, 40),
          note: 'Показаны максимум 40 методов. Используй search для уточнения.',
        });
      } catch (error) {
        return failure(error);
      }
    },
  );

  // Инструмент №2: вызов любого разрешённого GET API.
  server.registerTool(
    'call_api',
    {
      description:
        'Выполнить GET-запрос к существующему REST API CIjhipster. ' +
        'Разрешён только путь, присутствующий в OpenAPI и прошедший проверку безопасности. ' +
        'Применяй для получения реальных данных приложения.',
      inputSchema: z.object({
        path: z.string().startsWith('/api/'),
        pathParams: z.record(z.string(), z.string()).default({}),
        query: z.record(z.string(), z.string()).default({}),
      }),
    },
    async ({ path, pathParams, query }) => {
      try {
        const token = process.env.CIJHIPSTER_TOKEN;

        if (!token) {
          throw new Error('Не настроен CIJHIPSTER_TOKEN');
        }

        const spec = await loadSpec();

        if (!allowed(path) || !spec.paths?.[path]?.get) {
          throw new Error('Этот GET endpoint не разрешён или отсутствует в OpenAPI');
        }

        const keys = [...path.matchAll(/\{([^}]+)\}/g)].map(match => match[1]);

        if (keys.some(key => !(key in pathParams))) {
          throw new Error(`Не заполнены параметры пути: ${keys.join(', ')}`);
        }

        let resolvedPath = path;

        for (const key of keys) {
          resolvedPath = resolvedPath.replace(
            `{${key}}`,
            encodeURIComponent(pathParams[key]),
          );
        }

        const url = new URL(resolvedPath, BASE_URL);

        if (url.origin !== new URL(BASE_URL).origin) {
          throw new Error('Недопустимый адрес запроса');
        }

        for (const [key, value] of Object.entries(query)) {
          url.searchParams.set(key, value);
        }

        if (url.searchParams.has('size')) {
          const requested = Number(url.searchParams.get('size'));
          url.searchParams.set(
            'size',
            String(Number.isFinite(requested) ? Math.max(1, Math.min(20, requested)) : 10),
          );
        } else {
          url.searchParams.set('size', '10');
        }

        const response = await fetch(url, {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: 'application/json',
          },
          signal: AbortSignal.timeout(15000),
          redirect: 'error',
        });

        if (!response.ok) {
          throw new Error(`Backend вернул HTTP ${response.status}`);
        }

        const body = await response.text();

        if (body.length > 50000) {
          throw new Error('Ответ слишком большой. Уточни фильтры или уменьши размер страницы.');
        }

        return {
          content: [{
            type: 'text',
            text: body,
          }],
        };
      } catch (error) {
        return failure(error);
      }
    },
  );

  return server;
});
