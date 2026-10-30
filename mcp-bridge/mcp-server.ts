import { randomUUID } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { McpServer } from '@modelcontextprotocol/server';
import { serveStdio } from '@modelcontextprotocol/server/stdio';
import * as z from 'zod/v4';

const BASE_URL = process.env.CIJHIPSTER_API_URL ?? 'http://localhost:8088';

function getBackendToken(): string {
  const filePath = process.env.CIJHIPSTER_TOKEN_FILE;

  const token = filePath ? readFileSync(filePath, 'utf8').trim() : (process.env.CIJHIPSTER_TOKEN ?? '').trim();

  if (!token) {
    throw new Error('Не настроен JWT для CIjhipster API');
  }

  return token;
}
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

// Проверяем, что GET endpoint разрешён.
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

  return !blocked.some(
    prefix => path === prefix || path.startsWith(prefix + '/'),
  );
}

// Получение OpenAPI с backend.
async function fetchSpec(): Promise<ApiSpec> {
  const token = getBackendToken();

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

// Кэширование OpenAPI на 5 минут.
const OPENAPI_CACHE_TTL_MS = 5 * 60 * 1000;

let specCache: {
  value: ApiSpec;
  expiresAt: number;
} | undefined;

let specLoading: Promise<ApiSpec> | undefined;

async function loadSpec(): Promise<ApiSpec> {
  if (specCache && Date.now() < specCache.expiresAt) {
    return specCache.value;
  }

  // Параллельные вызовы используют одну загрузку.
  if (!specLoading) {
    specLoading = fetchSpec()
      .then(spec => {
        specCache = {
          value: spec,
          expiresAt: Date.now() + OPENAPI_CACHE_TTL_MS,
        };

        return spec;
      })
      .finally(() => {
        specLoading = undefined;
      });
  }

  return specLoading;
}

// -----------------------------------------------------
// CRUD для тестовых DataSource
// -----------------------------------------------------

const CRUD_ENABLED = process.env.CIJHIPSTER_MCP_ENABLE_WRITES === 'true';
const TEST_COMPETITOR_ID = Number(process.env.CIJHIPSTER_MCP_TEST_COMPETITOR_ID);
const TEST_PREFIX = 'MCP_CRUD_TEST_';

type TestSource = {
  url: string;
  competitorId: number;
  namePrefix: string;
};

// PUT и DELETE доступны только для записей,
// созданных текущим MCP-процессом.
const testSources = new Map<number, TestSource>();

type SourceResponse = {
  id: number;
  sourceName: string;
  url: string;
  sourceType: string;
  isActive: boolean;
  createdAt: string;
  lastCheckedAt?: string | null;
  competitor: {
    id: number;
  };
};

function assertCrudEnabled(): void {
  if (!CRUD_ENABLED) {
    throw new Error(
      'CRUD выключен. Установи CIJHIPSTER_MCP_ENABLE_WRITES=true.',
    );
  }

  if (
    !Number.isSafeInteger(TEST_COMPETITOR_ID) ||
    TEST_COMPETITOR_ID <= 0
  ) {
    throw new Error(
      'Укажи корректный CIJHIPSTER_MCP_TEST_COMPETITOR_ID.',
    );
  }
}

function assertTestId(id: number): TestSource {
  const entry = testSources.get(id);

  if (!entry) {
    throw new Error(
      'Изменять и удалять можно только источники, созданные этим MCP-процессом.',
    );
  }

  return entry;
}

async function readWriteSqlTrace(
  traceId: string | undefined,
): Promise<unknown> {
  if (!traceId) {
    return undefined;
  }

  try {
    const token = getBackendToken();

    const response = await fetch(
      new URL(
        `/api/mcp-sql-traces/${traceId}`,
        BASE_URL,
      ),
      {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'application/json',
        },
        signal: AbortSignal.timeout(10000),
        redirect: 'error',
      },
    );

    if (!response.ok) {
      return {
        requestId: traceId,
        error: `SQL trace HTTP ${response.status}`,
      };
    }

    const text = await response.text();

    if (text.length > 50000) {
      return {
        requestId: traceId,
        error: 'SQL-трассировка слишком большая',
      };
    }

    return JSON.parse(text);
  } catch (error) {
    return {
      requestId: traceId,
      error:
        error instanceof Error
          ? error.message
          : 'Ошибка получения SQL',
    };
  }
}


function asSource(value: unknown): SourceResponse {
  if (
    !value ||
    typeof value !== 'object' ||
    Array.isArray(value)
  ) {
    throw new Error(
      'Backend вернул неожиданный формат DataSource.',
    );
  }

  const r = value as Partial<SourceResponse>;

  if (
    !Number.isSafeInteger(r.id) ||
    typeof r.sourceName !== 'string' ||
    typeof r.url !== 'string' ||
    typeof r.sourceType !== 'string' ||
    typeof r.isActive !== 'boolean' ||
    typeof r.createdAt !== 'string' ||
    !r.competitor ||
    !Number.isSafeInteger(r.competitor.id)
  ) {
    throw new Error(
      'Backend вернул неполные данные DataSource.',
    );
  }

  return r as SourceResponse;
}

// HTTP-запросы для CRUD.
// Доступ ограничен маршрутом /api/data-sources.
async function sourceRequest(
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    path: string,
    data?: unknown,
    traceId?: string,
  ): Promise<{ status: number; body: unknown }> {
    const token = getBackendToken();

  if (!token) {
    throw new Error('Не настроен CIJHIPSTER_TOKEN');
  }

  if (!/^\/api\/data-sources(?:\/[1-9][0-9]*)?$/.test(path)) {
    throw new Error(
      'Операция разрешена только для /api/data-sources.',
    );
  }

  const url = new URL(path, BASE_URL);

  if (
    url.origin !== new URL(BASE_URL).origin ||
    url.pathname !== path
  ) {
    throw new Error('Недопустимый адрес запроса.');
  }

  const startedAt = performance.now();

  let response: Response;

  try {
    response = await fetch(url, {
      method,
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/json',
        ...(data === undefined
          ? {}
          : { 'Content-Type': 'application/json' }),
        ...(traceId
          ? { 'X-MCP-Trace-ID': traceId }
          : {}),
      },
      ...(data === undefined
        ? {}
        : { body: JSON.stringify(data) }),
      signal: AbortSignal.timeout(15000),
      redirect: 'error',
    });
  } catch (error) {
    console.error(JSON.stringify({
      event: 'mcp_crud_response',
      method,
      path,
      status: 'network_error',
      durationMs: Math.round(
        performance.now() - startedAt,
      ),
    }));

    throw error;
  }

  console.error(JSON.stringify({
    event: 'mcp_crud_response',
    timestamp: new Date().toISOString(),
    method,
    path,
    status: response.status,
    durationMs: Math.round(
      performance.now() - startedAt,
    ),
  }));

  if (!response.ok) {
    throw new Error(
      `${method} ${path}: Backend HTTP ${response.status}`,
    );
  }

  if (response.status === 204) {
    return {
      status: response.status,
      body: null,
    };
  }

  const raw = await response.text();

  if (raw.length > 50000) {
    throw new Error(
      'Ответ backend превышает ограничение размера.',
    );
  }

  let body: unknown;

  try {
    body = JSON.parse(raw);
  } catch {
    throw new Error('Backend вернул не-JSON ответ.');
  }

  return {
    status: response.status,
    body,
  };
}

// Проверка принадлежности тестового DataSource.
async function getOwnTestSource(
  id: number,
): Promise<SourceResponse> {
  const test = assertTestId(id);

  const { body } = await sourceRequest(
    'GET',
    `/api/data-sources/${id}`,
  );

  const source = asSource(body);

  if (
    source.id !== id ||
    source.url !== test.url ||
    !source.sourceName.startsWith(test.namePrefix) ||
    source.sourceType !== 'WEBSITE' ||
    source.isActive !== false ||
    source.competitor.id !== test.competitorId
  ) {
    throw new Error(
      'Проверка тестового источника не пройдена: PUT/DELETE заблокирован.',
    );
  }

  return source;
}

// -----------------------------------------------------
// Формирование ответов MCP
// -----------------------------------------------------

function success(data: unknown) {
  return {
    content: [{
      type: 'text' as const,
      text: JSON.stringify(data, null, 2),
    }],
  };
}

function failure(error: unknown) {
  return {
    isError: true,
    content: [{
      type: 'text' as const,
      text:
        error instanceof Error
          ? error.message
          : 'Неизвестная ошибка',
    }],
  };
}

// -----------------------------------------------------
// Запуск MCP-сервера
// -----------------------------------------------------

export function createCiJhipsterServer(): McpServer {  const server = new McpServer({
    name: 'cijhipster-mcp',
    version: '2.0.0',
  });

  // ---------------------------------------------------
  // Инструмент 1: explore_api
  // ---------------------------------------------------

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

        for (
          const [path, methods] of Object.entries(
            spec.paths ?? {},
          )
        ) {
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

          if (
            query &&
            !JSON.stringify(item)
              .toLowerCase()
              .includes(query)
          ) {
            continue;
          }

          endpoints.push(item);
        }

        return success({
          totalMatches: endpoints.length,
          endpoints: endpoints.slice(0, 40),
          note:
            'Показаны максимум 40 методов. ' +
            'Используй search для уточнения.',
        });
      } catch (error) {
        return failure(error);
      }
    },
  );

  // ---------------------------------------------------
  // Инструмент 2: call_api (только GET)
  // ---------------------------------------------------

  server.registerTool(
    'call_api',
    {
      description:
        'Выполнить GET-запрос к существующему REST API CIjhipster. ' +
        'Разрешён только путь, присутствующий в OpenAPI ' +
        'и прошедший проверку безопасности. ' +
        'Применяй для получения реальных данных приложения.',
      inputSchema: z.object({
        path: z.string().startsWith('/api/'),
        pathParams: z.record(z.string(), z.string()).default({}),
        query: z.record(z.string(), z.string()).default({}),
        showSql: z.boolean().default(false),
      }),
    },
    async ({ path, pathParams, query, showSql }) => {
      try {
        const token = getBackendToken();

        if (!token) {
          throw new Error(
            'Не настроен CIJHIPSTER_TOKEN',
          );
        }

        const spec = await loadSpec();

        if (
          !allowed(path) ||
          !spec.paths?.[path]?.get
        ) {
          throw new Error(
            'Этот GET endpoint не разрешён ' +
            'или отсутствует в OpenAPI',
          );
        }

        const keys = [
          ...path.matchAll(/\{([^}]+)\}/g),
        ].map(match => match[1]);

        if (
          keys.some(
            key => !(key in pathParams),
          )
        ) {
          throw new Error(
            `Не заполнены параметры пути: ${keys.join(', ')}`,
          );
        }

        let resolvedPath = path;

        for (const key of keys) {
          resolvedPath = resolvedPath.replace(
            `{${key}}`,
            encodeURIComponent(pathParams[key]),
          );
        }

        const url = new URL(
          resolvedPath,
          BASE_URL,
        );

        if (
          url.origin !== new URL(BASE_URL).origin
        ) {
          throw new Error(
            'Недопустимый адрес запроса',
          );
        }

        for (
          const [key, value] of Object.entries(query)
        ) {
          url.searchParams.set(key, value);
        }

        if (url.searchParams.has('size')) {
          const requested = Number(
            url.searchParams.get('size'),
          );

          url.searchParams.set(
            'size',
            String(
              Number.isFinite(requested)
                ? Math.max(
                    1,
                    Math.min(20, requested),
                  )
                : 10,
            ),
          );
        } else {
          url.searchParams.set('size', '10');
        }
        const sqlTraceId = showSql ? randomUUID() : undefined;

        const startedAt = performance.now();

        console.error(JSON.stringify({
          event: 'mcp_api_request',
          timestamp: new Date().toISOString(),
          method: 'GET',
          path: url.pathname,
          queryKeys: [
            ...url.searchParams.keys(),
          ],
        }));

        let response: Response;

        try {
          response = await fetch(url, {
            method: 'GET',
            headers: {
              Authorization: `Bearer ${token}`,
              Accept: 'application/json',
              ...(sqlTraceId
                ? { 'X-MCP-Trace-ID': sqlTraceId }
                : {}),
            },
            signal: AbortSignal.timeout(15000),
            redirect: 'error',
          });
        } catch (error) {
          console.error(JSON.stringify({
            event: 'mcp_api_response',
            timestamp: new Date().toISOString(),
            method: 'GET',
            path: url.pathname,
            status: 'network_error',
            durationMs: Math.round(
              performance.now() - startedAt,
            ),
          }));

          throw error;
        }

        console.error(JSON.stringify({
          event: 'mcp_api_response',
          timestamp: new Date().toISOString(),
          method: 'GET',
          path: url.pathname,
          status: response.status,
          durationMs: Math.round(
            performance.now() - startedAt,
          ),
        }));

        if (!response.ok) {
          throw new Error(
            `Backend вернул HTTP ${response.status}`,
          );
        }

        const body = await response.text();

        if (body.length > 50000) {
          throw new Error(
            'Ответ слишком большой. ' +
            'Уточни фильтры или уменьши размер страницы.',
          );
        }


        if (sqlTraceId) {
          let sqlTrace: unknown;

          try {
            const traceResponse = await fetch(
              new URL(
                `/api/mcp-sql-traces/${sqlTraceId}`,
                BASE_URL,
              ),
              {
                method: 'GET',
                headers: {
                  Authorization: `Bearer ${token}`,
                  Accept: 'application/json',
                },
                signal: AbortSignal.timeout(10000),
                redirect: 'error',
              },
            );

            if (!traceResponse.ok) {
              sqlTrace = {
                error:
                  `Не удалось получить SQL: ` +
                  `HTTP ${traceResponse.status}`,
              };
            } else {
              const traceBody = await traceResponse.text();

              if (traceBody.length > 50000) {
                sqlTrace = {
                  error: 'Ответ SQL-трассировки слишком большой',
                };
              } else {
                sqlTrace = JSON.parse(traceBody);
              }
            }
          } catch (error) {
            sqlTrace = {
              error:
                error instanceof Error
                  ? error.message
                  : 'Ошибка получения SQL',
            };
          }

          let parsedData: unknown;

          try {
            parsedData = JSON.parse(body);
          } catch {
            parsedData = body;
          }

          return success({
            data: parsedData,
            sqlTrace,
          });
        }


        return {
          content: [{
            type: 'text' as const,
            text: body,
          }],
        };
      } catch (error) {
        return failure(error);
      }
    },
  );

  // ---------------------------------------------------
  // Инструмент 3: create_data_source
  // ---------------------------------------------------

  server.registerTool(
    'create_data_source',
    {
      description:
        'Создать только неактивный тестовый DataSource ' +
        '(WEBSITE, example.com). Изменяет данные. ' +
        'Перед выполнением запроси согласие пользователя. ' +
        'Требуется право SOURCES_EDIT на конкурента.',
      inputSchema: z.object({
        nameSuffix: z.string()
          .min(1)
          .max(70)
          .regex(/^[\p{L}\p{N} _-]+$/u)
          .default('Source'),

        showSql: z.boolean().default(false),
      }),
    },
    async ({ nameSuffix, showSql }) => {
      try {
        assertCrudEnabled();

        const marker = randomUUID().replaceAll('-', '');

        const namePrefix =
          `${TEST_PREFIX}${marker.slice(0, 12)}_`;

        const data = {
          sourceName: namePrefix + nameSuffix,
          url:
            `https://example.com/mcp-crud-test/${marker}`,
          sourceType: 'WEBSITE',
          isActive: false,
          createdAt: new Date().toISOString(),
          competitor: {
            id: TEST_COMPETITOR_ID,
          },
        };

        const traceId = showSql ? randomUUID() : undefined;

        const { status, body } = await sourceRequest(
          'POST',
          '/api/data-sources',
          data,
          traceId,
        );

        const created = asSource(body);

        if (
          status !== 201 ||
          created.url !== data.url ||
          created.competitor.id !== TEST_COMPETITOR_ID
        ) {
          throw new Error(
            'Ответ создания не прошёл проверку. ' +
            'Проверь запись вручную перед следующим действием.',
          );
        }

        testSources.set(created.id, {
          url: data.url,
          competitorId: TEST_COMPETITOR_ID,
          namePrefix,
        });

        return success({
          status,
          created,
          note:
            'Это неактивный тестовый источник. ' +
            'ID доступен для изменения и удаления ' +
            'до завершения процесса MCP.',
          ...(showSql
            ? { sqlTrace: await readWriteSqlTrace(traceId) }
            : {}),
        });

      } catch (error) {
        return failure(error);
      }
    },
  );

  // ---------------------------------------------------
  // Инструмент 4: update_data_source
  // ---------------------------------------------------

  server.registerTool(
    'update_data_source',
    {
      description:
        'Изменить название только DataSource, ' +
        'созданного этим MCP-процессом. ' +
        'Использует PUT. Не изменяет URL, тип, ' +
        'привязку и isActive=false. ' +
        'Перед выполнением запроси согласие пользователя.',
      inputSchema: z.object({
        id: z.number()
          .int()
          .positive(),
        nameSuffix: z.string()
          .min(1)
          .max(70)
          .regex(/^[\p{L}\p{N} _-]+$/u),
        showSql: z.boolean().default(false),
      }),
    },
    async ({ id, nameSuffix, showSql }) => {
      try {
        assertCrudEnabled();

        const current = await getOwnTestSource(id);
        const test = assertTestId(id);

        const payload = {
          id,
          sourceName:
            test.namePrefix + nameSuffix,
          url: current.url,
          sourceType: current.sourceType,
          isActive: false,
          createdAt: current.createdAt,
          lastCheckedAt:
            current.lastCheckedAt ?? null,
          competitor: {
            id: TEST_COMPETITOR_ID,
          },
        };

        const traceId = showSql ? randomUUID() : undefined;

        const { status, body } = await sourceRequest(
          'PUT',
          `/api/data-sources/${id}`,
          payload,
          traceId,
        );

        return success({
          status,
          updated: asSource(body),
          ...(showSql
            ? { sqlTrace: await readWriteSqlTrace(traceId) }
            : {}),
        });
      } catch (error) {
        return failure(error);
      }
    },
  );

  // ---------------------------------------------------
  // Инструмент 5: delete_data_source
  // ---------------------------------------------------

  server.registerTool(
    'delete_data_source',
    {
      description:
        'Безвозвратно удалить только тестовый DataSource, ' +
        'созданный этим MCP-процессом. ' +
        'Сначала получи явное подтверждение пользователя, ' +
        'затем передай confirmation в формате DELETE <id>. ' +
        'Требуется ручное разрешение инструмента в OpenCode.',
      inputSchema: z.object({
        id: z.number()
          .int()
          .positive(),
        confirmation: z.string(),
        showSql: z.boolean().default(false),
      }),
    },
    async ({ id, confirmation, showSql }) => {
      try {
        assertCrudEnabled();

        if (confirmation !== `DELETE ${id}`) {
          throw new Error(
            `Удаление требует подтверждения: DELETE ${id}`,
          );
        }

        await getOwnTestSource(id);

        const traceId = showSql ? randomUUID() : undefined;

        const { status } = await sourceRequest(
          'DELETE',
          `/api/data-sources/${id}`,
          undefined,
          traceId,
        );

        if (status !== 204) {
          throw new Error(
            `Неожиданный HTTP ${status} при удалении.`,
          );
        }

        testSources.delete(id);
        return success({
          status,
          deletedId: id,
          ...(showSql
            ? { sqlTrace: await readWriteSqlTrace(traceId) }
            : {}),
        });
      } catch (error) {
        return failure(error);
      }
    },
  );

   return server;
}

if (process.env.CIJHIPSTER_MCP_TRANSPORT !== 'http') {
  serveStdio(createCiJhipsterServer);
}