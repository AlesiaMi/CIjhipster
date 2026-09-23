import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
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

@Injectable({
  providedIn: 'root',
})
export class EntityImportService {
  private readonly http = inject(HttpClient);

  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly managerClientContext = inject(ManagerClientContextService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/import');

  importFile(entityType: string, file: File): Observable<ImportResult> {
    const formData = new FormData();

    formData.append('file', file);

    const params = createRequestOption(this.managerClientContext.withClientUserId({}));

    return this.http.post<ImportResult>(`${this.resourceUrl}/${encodeURIComponent(entityType)}`, formData, { params });
  }
}
