import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IAnalysisResult, NewAnalysisResult } from '../analysis-result.model';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';

export type PartialUpdateAnalysisResult = Partial<IAnalysisResult> & Pick<IAnalysisResult, 'id'>;

type RestOf<T extends IAnalysisResult | NewAnalysisResult> = Omit<T, 'analyzedAt'> & {
  analyzedAt?: string | null;
};

export type RestAnalysisResult = RestOf<IAnalysisResult>;

export type NewRestAnalysisResult = RestOf<NewAnalysisResult>;

export type PartialUpdateRestAnalysisResult = RestOf<PartialUpdateAnalysisResult>;

@Injectable()
export class AnalysisResultsService {
  readonly analysisResultsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly analysisResultsResource = httpResource<RestAnalysisResult[]>(() => {
    const params = this.analysisResultsParams();
    if (!params) {
      return undefined;
    }
    if (!this.managerClientContext.initialized()) {
      return undefined;
    }

    if (this.managerClientContext.isManager() && this.managerClientContext.selectedClientUserId() === null) {
      return undefined;
    }

    return { url: this.resourceUrl, params: this.managerClientContext.withClientUserId(params) };
  });

  readonly analysisResults = computed(() =>
    (this.analysisResultsResource.hasValue() ? this.analysisResultsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/analysis-results');
  protected readonly managerClientContext = inject(ManagerClientContextService);
  protected convertValueFromServer(restAnalysisResult: RestAnalysisResult): IAnalysisResult {
    return {
      ...restAnalysisResult,
      analyzedAt: restAnalysisResult.analyzedAt ? dayjs(restAnalysisResult.analyzedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class AnalysisResultService extends AnalysisResultsService {
  protected readonly http = inject(HttpClient);

  create(analysisResult: NewAnalysisResult): Observable<IAnalysisResult> {
    const copy = this.convertValueFromClient(analysisResult);
    return this.http.post<RestAnalysisResult>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(analysisResult: IAnalysisResult): Observable<IAnalysisResult> {
    const copy = this.convertValueFromClient(analysisResult);
    return this.http
      .put<RestAnalysisResult>(`${this.resourceUrl}/${encodeURIComponent(this.getAnalysisResultIdentifier(analysisResult))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(analysisResult: PartialUpdateAnalysisResult): Observable<IAnalysisResult> {
    const copy = this.convertValueFromClient(analysisResult);
    return this.http
      .patch<RestAnalysisResult>(`${this.resourceUrl}/${encodeURIComponent(this.getAnalysisResultIdentifier(analysisResult))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IAnalysisResult> {
    return this.http
      .get<RestAnalysisResult>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IAnalysisResult[]>> {
    const options = createRequestOption(this.managerClientContext.withClientUserId(req ?? {}));
    return this.http
      .get<RestAnalysisResult[]>(this.resourceUrl, {
        params: options,
        observe: 'response',
      })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getAnalysisResultIdentifier(analysisResult: Pick<IAnalysisResult, 'id'>): number {
    return analysisResult.id;
  }

  compareAnalysisResult(o1: Pick<IAnalysisResult, 'id'> | null, o2: Pick<IAnalysisResult, 'id'> | null): boolean {
    return o1 && o2 ? this.getAnalysisResultIdentifier(o1) === this.getAnalysisResultIdentifier(o2) : o1 === o2;
  }

  addAnalysisResultToCollectionIfMissing<Type extends Pick<IAnalysisResult, 'id'>>(
    analysisResultCollection: Type[],
    ...analysisResultsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const analysisResults: Type[] = analysisResultsToCheck.filter(isPresent);
    if (analysisResults.length > 0) {
      const analysisResultCollectionIdentifiers = analysisResultCollection.map(analysisResultItem =>
        this.getAnalysisResultIdentifier(analysisResultItem),
      );
      const analysisResultsToAdd = analysisResults.filter(analysisResultItem => {
        const analysisResultIdentifier = this.getAnalysisResultIdentifier(analysisResultItem);
        if (analysisResultCollectionIdentifiers.includes(analysisResultIdentifier)) {
          return false;
        }
        analysisResultCollectionIdentifiers.push(analysisResultIdentifier);
        return true;
      });
      return [...analysisResultsToAdd, ...analysisResultCollection];
    }
    return analysisResultCollection;
  }

  protected convertValueFromClient<T extends IAnalysisResult | NewAnalysisResult | PartialUpdateAnalysisResult>(
    analysisResult: T,
  ): RestOf<T> {
    return {
      ...analysisResult,
      analyzedAt: analysisResult.analyzedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestAnalysisResult): IAnalysisResult {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestAnalysisResult[]): IAnalysisResult[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
