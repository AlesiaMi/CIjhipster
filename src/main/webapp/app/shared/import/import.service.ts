import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

import { serverApiUrl } from 'app/config/endpoint.constants';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';
import { createRequestOption } from 'app/core/request/request-util';

export interface ImportError {
  row?: number | null;
  field?: string | null;
  message: string;
}

export interface ImportResult {
  total: number;
  imported: number;
  errors: ImportError[];
}

export interface ImportProgress {
  type: 'progress';
  importId: string;
  phase: 'PARSING' | 'VALIDATING' | 'SAVING' | 'COMMITTING' | 'COMPLETED' | 'FAILED';
  processed: number;
  total: number;
  percent: number;
  message: string;
}

export type ImportActivity =
  { kind: 'progress'; progress: ImportProgress } | { kind: 'connection-lost' } | { kind: 'result'; result: ImportResult };

@Injectable({
  providedIn: 'root',
})
export class EntityImportService {
  private readonly http = inject(HttpClient);
  private readonly managerClientContext = inject(ManagerClientContextService);
  private readonly stateStorage = inject(StateStorageService);

  private readonly resourceUrl = `${serverApiUrl}api/import`;

  importFile(entityType: string, file: File, bulk = false, importId?: string): Observable<ImportResult> {
    const formData = new FormData();
    formData.append('file', file);

    let params = createRequestOption(this.managerClientContext.withClientUserId({ bulk }));

    if (importId) {
      params = params.set('importId', importId);
    }

    return this.http.post<ImportResult>(`${this.resourceUrl}/${encodeURIComponent(entityType)}`, formData, { params });
  }

  importBulkWithProgress(entityType: string, file: File): Observable<ImportActivity> {
    return new Observable<ImportActivity>(observer => {
      const importId = crypto.randomUUID();
      const token = this.stateStorage.getAuthenticationToken();

      let socket: WebSocket | undefined;
      let request: Subscription | undefined;
      let connectionTimeout: ReturnType<typeof setTimeout> | undefined;

      let uploadStarted = false;
      let finished = false;
      let connectionErrorShown = false;

      const clearConnectionTimeout = (): void => {
        if (connectionTimeout !== undefined) {
          clearTimeout(connectionTimeout);
          connectionTimeout = undefined;
        }
      };

      const startUpload = (): void => {
        if (uploadStarted || finished) {
          return;
        }

        uploadStarted = true;

        request = this.importFile(entityType, file, true, importId).subscribe({
          next(result) {
            finished = true;
            observer.next({ kind: 'result', result });
            observer.complete();
          },
          error(error) {
            finished = true;
            observer.error(error);
          },
        });
      };

      const socketUnavailable = (): void => {
        clearConnectionTimeout();

        if (!finished && !connectionErrorShown) {
          connectionErrorShown = true;
          observer.next({ kind: 'connection-lost' });
        }

        // WebSocket не должен блокировать сам импорт.
        startUpload();
      };

      if (!token) {
        socketUnavailable();
      } else {
        try {
          const socketUrl = new URL(`${this.resourceUrl}/progress/ws`, document.baseURI);
          socketUrl.protocol = socketUrl.protocol === 'https:' ? 'wss:' : 'ws:';

          socket = new WebSocket(socketUrl.toString());
          const currentSocket = socket;

          connectionTimeout = setTimeout(() => {
            currentSocket.close();
            socketUnavailable();
          }, 5000);

          currentSocket.onopen = () => {
            currentSocket.send(
              JSON.stringify({
                type: 'authenticate',
                token,
              }),
            );
          };

          currentSocket.onmessage = (event: MessageEvent<string>) => {
            let message: unknown;

            try {
              message = JSON.parse(event.data);
            } catch {
              return;
            }

            if (typeof message !== 'object' || message === null || !('type' in message)) {
              return;
            }

            const data = message as { type: string; importId?: string };

            if (data.type === 'ready') {
              clearConnectionTimeout();
              startUpload();
              return;
            }

            if (data.type === 'progress' && data.importId === importId && !finished) {
              observer.next({
                kind: 'progress',
                progress: message as ImportProgress,
              });
            }
          };

          currentSocket.onerror = socketUnavailable;

          currentSocket.onclose = () => {
            if (!finished) {
              socketUnavailable();
            }
          };
        } catch {
          socketUnavailable();
        }
      }

      return () => {
        finished = true;
        clearConnectionTimeout();
        request?.unsubscribe();
        socket?.close();
      };
    });
  }
}
