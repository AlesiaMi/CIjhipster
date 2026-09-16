import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IDataSource, NewDataSource } from '../data-source.model';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';

export type PartialUpdateDataSource = Partial<IDataSource> & Pick<IDataSource, 'id'>;

type RestOf<T extends IDataSource | NewDataSource> = Omit<T, 'lastCheckedAt' | 'createdAt'> & {
  lastCheckedAt?: string | null;
  createdAt?: string | null;
};

export type RestDataSource = RestOf<IDataSource>;

export type NewRestDataSource = RestOf<NewDataSource>;

export type PartialUpdateRestDataSource = RestOf<PartialUpdateDataSource>;

@Injectable()
export class DataSourcesService {
  protected readonly managerClientContext = inject(ManagerClientContextService);
  readonly dataSourcesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly dataSourcesResource = httpResource<RestDataSource[]>(() => {
    const params = this.dataSourcesParams();

    if (!params || !this.managerClientContext.initialized()) {
      return undefined;
    }

    if (this.managerClientContext.isManager() && this.managerClientContext.selectedClientUserId() === null) {
      return undefined;
    }

    return {
      url: this.resourceUrl,
      params: this.managerClientContext.withClientUserId(params),
    };
  });
  /**
   * This signal holds the list of dataSource that have been fetched. It is updated when the dataSourcesResource emits a new value.
   * In case of error while fetching the dataSources, the signal is set to an empty array.
   */
  readonly dataSources = computed(() =>
    (this.dataSourcesResource.hasValue() ? this.dataSourcesResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/data-sources');

  protected convertValueFromServer(restDataSource: RestDataSource): IDataSource {
    return {
      ...restDataSource,
      lastCheckedAt: restDataSource.lastCheckedAt ? dayjs(restDataSource.lastCheckedAt) : undefined,
      createdAt: restDataSource.createdAt ? dayjs(restDataSource.createdAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class DataSourceService extends DataSourcesService {
  protected readonly http = inject(HttpClient);

  create(dataSource: NewDataSource): Observable<IDataSource> {
    const copy = this.convertValueFromClient(dataSource);
    return this.http.post<RestDataSource>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(dataSource: IDataSource): Observable<IDataSource> {
    const copy = this.convertValueFromClient(dataSource);
    return this.http
      .put<RestDataSource>(`${this.resourceUrl}/${encodeURIComponent(this.getDataSourceIdentifier(dataSource))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(dataSource: PartialUpdateDataSource): Observable<IDataSource> {
    const copy = this.convertValueFromClient(dataSource);
    return this.http
      .patch<RestDataSource>(`${this.resourceUrl}/${encodeURIComponent(this.getDataSourceIdentifier(dataSource))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IDataSource> {
    return this.http
      .get<RestDataSource>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IDataSource[]>> {
    const options = createRequestOption(this.managerClientContext.withClientUserId(req ?? {}));
    return this.http
      .get<RestDataSource[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getDataSourceIdentifier(dataSource: Pick<IDataSource, 'id'>): number {
    return dataSource.id;
  }

  compareDataSource(o1: Pick<IDataSource, 'id'> | null, o2: Pick<IDataSource, 'id'> | null): boolean {
    return o1 && o2 ? this.getDataSourceIdentifier(o1) === this.getDataSourceIdentifier(o2) : o1 === o2;
  }

  addDataSourceToCollectionIfMissing<Type extends Pick<IDataSource, 'id'>>(
    dataSourceCollection: Type[],
    ...dataSourcesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const dataSources: Type[] = dataSourcesToCheck.filter(isPresent);
    if (dataSources.length > 0) {
      const dataSourceCollectionIdentifiers = dataSourceCollection.map(dataSourceItem => this.getDataSourceIdentifier(dataSourceItem));
      const dataSourcesToAdd = dataSources.filter(dataSourceItem => {
        const dataSourceIdentifier = this.getDataSourceIdentifier(dataSourceItem);
        if (dataSourceCollectionIdentifiers.includes(dataSourceIdentifier)) {
          return false;
        }
        dataSourceCollectionIdentifiers.push(dataSourceIdentifier);
        return true;
      });
      return [...dataSourcesToAdd, ...dataSourceCollection];
    }
    return dataSourceCollection;
  }

  protected convertValueFromClient<T extends IDataSource | NewDataSource | PartialUpdateDataSource>(dataSource: T): RestOf<T> {
    return {
      ...dataSource,
      lastCheckedAt: dataSource.lastCheckedAt?.toJSON() ?? null,
      createdAt: dataSource.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestDataSource): IDataSource {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestDataSource[]): IDataSource[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
