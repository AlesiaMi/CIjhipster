import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { INewsItem, NewNewsItem } from '../news-item.model';

export type PartialUpdateNewsItem = Partial<INewsItem> & Pick<INewsItem, 'id'>;

type RestOf<T extends INewsItem | NewNewsItem> = Omit<T, 'publishedAt' | 'collectedAt'> & {
  publishedAt?: string | null;
  collectedAt?: string | null;
};

export type RestNewsItem = RestOf<INewsItem>;

export type NewRestNewsItem = RestOf<NewNewsItem>;

export type PartialUpdateRestNewsItem = RestOf<PartialUpdateNewsItem>;

@Injectable()
export class NewsItemsService {
  readonly newsItemsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly newsItemsResource = httpResource<RestNewsItem[]>(() => {
    const params = this.newsItemsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of newsItem that have been fetched. It is updated when the newsItemsResource emits a new value.
   * In case of error while fetching the newsItems, the signal is set to an empty array.
   */
  readonly newsItems = computed(() =>
    (this.newsItemsResource.hasValue() ? this.newsItemsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/news-items');

  protected convertValueFromServer(restNewsItem: RestNewsItem): INewsItem {
    return {
      ...restNewsItem,
      publishedAt: restNewsItem.publishedAt ? dayjs(restNewsItem.publishedAt) : undefined,
      collectedAt: restNewsItem.collectedAt ? dayjs(restNewsItem.collectedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class NewsItemService extends NewsItemsService {
  protected readonly http = inject(HttpClient);

  create(newsItem: NewNewsItem): Observable<INewsItem> {
    const copy = this.convertValueFromClient(newsItem);
    return this.http.post<RestNewsItem>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(newsItem: INewsItem): Observable<INewsItem> {
    const copy = this.convertValueFromClient(newsItem);
    return this.http
      .put<RestNewsItem>(`${this.resourceUrl}/${encodeURIComponent(this.getNewsItemIdentifier(newsItem))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(newsItem: PartialUpdateNewsItem): Observable<INewsItem> {
    const copy = this.convertValueFromClient(newsItem);
    return this.http
      .patch<RestNewsItem>(`${this.resourceUrl}/${encodeURIComponent(this.getNewsItemIdentifier(newsItem))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<INewsItem> {
    return this.http
      .get<RestNewsItem>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<INewsItem[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestNewsItem[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getNewsItemIdentifier(newsItem: Pick<INewsItem, 'id'>): number {
    return newsItem.id;
  }

  compareNewsItem(o1: Pick<INewsItem, 'id'> | null, o2: Pick<INewsItem, 'id'> | null): boolean {
    return o1 && o2 ? this.getNewsItemIdentifier(o1) === this.getNewsItemIdentifier(o2) : o1 === o2;
  }

  addNewsItemToCollectionIfMissing<Type extends Pick<INewsItem, 'id'>>(
    newsItemCollection: Type[],
    ...newsItemsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const newsItems: Type[] = newsItemsToCheck.filter(isPresent);
    if (newsItems.length > 0) {
      const newsItemCollectionIdentifiers = newsItemCollection.map(newsItemItem => this.getNewsItemIdentifier(newsItemItem));
      const newsItemsToAdd = newsItems.filter(newsItemItem => {
        const newsItemIdentifier = this.getNewsItemIdentifier(newsItemItem);
        if (newsItemCollectionIdentifiers.includes(newsItemIdentifier)) {
          return false;
        }
        newsItemCollectionIdentifiers.push(newsItemIdentifier);
        return true;
      });
      return [...newsItemsToAdd, ...newsItemCollection];
    }
    return newsItemCollection;
  }

  protected convertValueFromClient<T extends INewsItem | NewNewsItem | PartialUpdateNewsItem>(newsItem: T): RestOf<T> {
    return {
      ...newsItem,
      publishedAt: newsItem.publishedAt?.toJSON() ?? null,
      collectedAt: newsItem.collectedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestNewsItem): INewsItem {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestNewsItem[]): INewsItem[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
