import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable, catchError, finalize, map, of, shareReplay, switchMap, tap, throwError } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';

export type ManagerPermission = 'VIEW' | 'COMPETITORS_EDIT' | 'SOURCES_EDIT' | 'COLLECTION_RUN' | 'NEWS_VIEW' | 'ANALYSIS_VIEW';

export interface ManagerClient {
  userId: number;
  login: string;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  permissions: ManagerPermission[];
}

export type ManagerRequestParams = Record<string, string | number | boolean | readonly (string | number | boolean)[]>;

const STORAGE_KEY = 'promai.manager.clientUserId';

@Injectable({ providedIn: 'root' })
export class ManagerClientContextService {
  private readonly http = inject(HttpClient);
  private readonly accountService = inject(AccountService);

  private initialization$?: Observable<void>;

  readonly initialized = signal(false);
  readonly loading = signal(false);

  readonly isManager = signal(false);

  readonly clients = signal<ManagerClient[]>([]);

  readonly selectedClientUserId = signal<number | null>(null);

  readonly selectedClient = computed(() => {
    const selectedId = this.selectedClientUserId();

    if (selectedId === null) {
      return null;
    }

    return this.clients().find(client => client.userId === selectedId) ?? null;
  });

  initialize(): Observable<void> {
    if (this.initialized()) {
      return of(undefined);
    }

    if (this.initialization$) {
      return this.initialization$;
    }

    this.loading.set(true);

    this.initialization$ = this.accountService.identity().pipe(
      switchMap(account => {
        const isManager = account?.authorities.includes('ROLE_MANAGER') ?? false;

        this.isManager.set(isManager);

        if (!isManager) {
          this.clients.set([]);
          this.selectedClientUserId.set(null);

          return of([] as ManagerClient[]);
        }

        return this.http.get<ManagerClient[]>('/api/manager/clients');
      }),

      tap(clients => {
        if (!this.isManager()) {
          return;
        }

        this.clients.set(clients);

        const storedValue = globalThis.sessionStorage.getItem(STORAGE_KEY);

        const storedId = storedValue !== null ? Number(storedValue) : null;

        const selectedId = clients.find(client => client.userId === storedId)?.userId ?? clients[0]?.userId ?? null;

        this.selectedClientUserId.set(selectedId);

        if (selectedId !== null) {
          globalThis.sessionStorage.setItem(STORAGE_KEY, selectedId.toString());
        } else {
          globalThis.sessionStorage.removeItem(STORAGE_KEY);
        }
      }),

      map(() => undefined),

      tap(() => {
        this.initialized.set(true);
      }),

      catchError(error => {
        this.initialization$ = undefined;
        return throwError(() => error);
      }),

      finalize(() => {
        this.loading.set(false);
      }),

      shareReplay({
        bufferSize: 1,
        refCount: false,
      }),
    );

    return this.initialization$;
  }

  selectClient(userId: number | null): void {
    if (!this.isManager()) {
      return;
    }

    if (userId === null) {
      this.selectedClientUserId.set(null);

      globalThis.sessionStorage.removeItem(STORAGE_KEY);

      return;
    }

    const exists = this.clients().some(client => client.userId === userId);

    if (!exists) {
      return;
    }

    this.selectedClientUserId.set(userId);

    globalThis.sessionStorage.setItem(STORAGE_KEY, userId.toString());
  }

  hasPermission(permission: ManagerPermission): boolean {
    if (!this.initialized()) {
      return false;
    }

    if (!this.isManager()) {
      return true;
    }

    return this.selectedClient()?.permissions.includes(permission) ?? false;
  }

  withClientUserId(params: ManagerRequestParams = {}): ManagerRequestParams {
    const result: ManagerRequestParams = {
      ...params,
    };

    if (this.isManager() && this.selectedClientUserId() !== null) {
      result['clientUserId'] = this.selectedClientUserId()!;
    }

    return result;
  }
}
