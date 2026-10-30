import { createServer } from 'node:http';
import { readFileSync } from 'node:fs';
import { timingSafeEqual } from 'node:crypto';
import { createMcpHandler } from '@modelcontextprotocol/server';
import { toNodeHandler } from '@modelcontextprotocol/node';
import { createCiJhipsterServer } from './mcp-server.js';

const port = Number(process.env.MCP_HTTP_PORT ?? '8765');

if (!Number.isInteger(port) || port < 1 || port > 65535) {
  throw new Error('Некорректный MCP_HTTP_PORT');
}

const keyPath = process.env.CIJHIPSTER_MCP_KEY_FILE;

if (!keyPath) {
  throw new Error('Не настроен CIJHIPSTER_MCP_KEY_FILE');
}

const key = readFileSync(keyPath, 'utf8').trim();

if (!/^[0-9a-f]{64}$/.test(key)) {
  throw new Error('Некорректный ключ доступа MCP');
}

const expectedKey = Buffer.from(key);

function authorized(header: string | undefined): boolean {
  if (!header?.startsWith('Bearer ')) {
    return false;
  }

  const candidate = Buffer.from(header.slice(7));

  return (
    candidate.length === expectedKey.length &&
    timingSafeEqual(candidate, expectedKey)
  );
}

const mcp = createMcpHandler(createCiJhipsterServer);
const nodeHandler = toNodeHandler(mcp);

const server = createServer((req, res) => {
  const host = req.headers.host?.toLowerCase() ?? '';

  const allowedHosts = [
    `127.0.0.1:${port}`,
    `localhost:${port}`,
  ];

  if (!allowedHosts.includes(host)) {
    res.writeHead(403).end('Forbidden host');
    return;
  }

  const origin = req.headers.origin;

  if (
    origin &&
    ![
      `http://127.0.0.1:${port}`,
      `http://localhost:${port}`,
    ].includes(origin)
  ) {
    res.writeHead(403).end('Forbidden origin');
    return;
  }

  if (req.url === '/health' && req.method === 'GET') {
    res.writeHead(200, {
      'Content-Type': 'text/plain',
    }).end('ok');
    return;
  }

  if (req.url !== '/mcp') {
    res.writeHead(404).end('Not found');
    return;
  }

  if (!authorized(req.headers.authorization)) {
    res.writeHead(401, {
      'WWW-Authenticate': 'Bearer',
    }).end('Unauthorized');
    return;
  }

  Promise.resolve(nodeHandler(req, res)).catch(error => {
    console.error(
      'MCP HTTP request failed:',
      error instanceof Error ? error.message : 'unknown',
    );

    if (!res.headersSent) {
      res.writeHead(500);
    }

    if (!res.writableEnded) {
      res.end();
    }
  });
});

server.listen(port, '0.0.0.0', () => {
  console.error(`CIjhipster MCP HTTP listening on ${port}`);
});

process.on('SIGTERM', () => {
  server.close(() => {
    void mcp.close().then(() => process.exit(0));
  });
});
