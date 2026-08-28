import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ICollectionRun, NewCollectionRun } from '../collection-run.model';

export type PartialUpdateCollectionRun = Partial<ICollectionRun> & Pick<ICollectionRun, 'id'>;

type RestOf<T extends ICollectionRun | NewCollectionRun> = Omit<T, 'startedAt' | 'finishedAt'> & {
  startedAt?: string | null;
  finishedAt?: string | null;
};

export type RestCollectionRun = RestOf<ICollectionRun>;

export type NewRestCollectionRun = RestOf<NewCollectionRun>;

export type PartialUpdateRestCollectionRun = RestOf<PartialUpdateCollectionRun>;

@Injectable()
export class CollectionRunsService {
  readonly collectionRunsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly collectionRunsResource = httpResource<RestCollectionRun[]>(() => {
    const params = this.collectionRunsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });

  readonly collectionRuns = computed(() =>
    (this.collectionRunsResource.hasValue() ? this.collectionRunsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/collection-runs');

  protected convertValueFromServer(restCollectionRun: RestCollectionRun): ICollectionRun {
    return {
      ...restCollectionRun,
      startedAt: restCollectionRun.startedAt ? dayjs(restCollectionRun.startedAt) : undefined,
      finishedAt: restCollectionRun.finishedAt ? dayjs(restCollectionRun.finishedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class CollectionRunService extends CollectionRunsService {
  protected readonly http = inject(HttpClient);

  create(collectionRun: NewCollectionRun): Observable<ICollectionRun> {
    const copy = this.convertValueFromClient(collectionRun);
    return this.http.post<RestCollectionRun>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(collectionRun: ICollectionRun): Observable<ICollectionRun> {
    const copy = this.convertValueFromClient(collectionRun);
    return this.http
      .put<RestCollectionRun>(`${this.resourceUrl}/${encodeURIComponent(this.getCollectionRunIdentifier(collectionRun))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(collectionRun: PartialUpdateCollectionRun): Observable<ICollectionRun> {
    const copy = this.convertValueFromClient(collectionRun);
    return this.http
      .patch<RestCollectionRun>(`${this.resourceUrl}/${encodeURIComponent(this.getCollectionRunIdentifier(collectionRun))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<ICollectionRun> {
    return this.http
      .get<RestCollectionRun>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<ICollectionRun[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestCollectionRun[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  runCollection(): Observable<void> {
    return this.http.post<void>(`${this.resourceUrl}/run`, {});
  }

  getCollectionRunIdentifier(collectionRun: Pick<ICollectionRun, 'id'>): number {
    return collectionRun.id;
  }

  compareCollectionRun(o1: Pick<ICollectionRun, 'id'> | null, o2: Pick<ICollectionRun, 'id'> | null): boolean {
    return o1 && o2 ? this.getCollectionRunIdentifier(o1) === this.getCollectionRunIdentifier(o2) : o1 === o2;
  }

  addCollectionRunToCollectionIfMissing<Type extends Pick<ICollectionRun, 'id'>>(
    collectionRunCollection: Type[],
    ...collectionRunsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const collectionRuns: Type[] = collectionRunsToCheck.filter(isPresent);
    if (collectionRuns.length > 0) {
      const collectionRunCollectionIdentifiers = collectionRunCollection.map(collectionRunItem =>
        this.getCollectionRunIdentifier(collectionRunItem),
      );
      const collectionRunsToAdd = collectionRuns.filter(collectionRunItem => {
        const collectionRunIdentifier = this.getCollectionRunIdentifier(collectionRunItem);
        if (collectionRunCollectionIdentifiers.includes(collectionRunIdentifier)) {
          return false;
        }
        collectionRunCollectionIdentifiers.push(collectionRunIdentifier);
        return true;
      });
      return [...collectionRunsToAdd, ...collectionRunCollection];
    }
    return collectionRunCollection;
  }

  protected convertValueFromClient<T extends ICollectionRun | NewCollectionRun | PartialUpdateCollectionRun>(collectionRun: T): RestOf<T> {
    return {
      ...collectionRun,
      startedAt: collectionRun.startedAt?.toJSON() ?? null,
      finishedAt: collectionRun.finishedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestCollectionRun): ICollectionRun {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestCollectionRun[]): ICollectionRun[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
