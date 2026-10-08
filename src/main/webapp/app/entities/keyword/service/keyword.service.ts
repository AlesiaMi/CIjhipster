import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IKeyword, NewKeyword } from '../keyword.model';

export type PartialUpdateKeyword = Partial<IKeyword> & Pick<IKeyword, 'id'>;

type RestOf<T extends IKeyword | NewKeyword> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestKeyword = RestOf<IKeyword>;

export type NewRestKeyword = RestOf<NewKeyword>;

export type PartialUpdateRestKeyword = RestOf<PartialUpdateKeyword>;

@Injectable()
export class KeywordsService {
  readonly keywordsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly keywordsResource = httpResource<RestKeyword[]>(() => {
    const params = this.keywordsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of keyword that have been fetched. It is updated when the keywordsResource emits a new value.
   * In case of error while fetching the keywords, the signal is set to an empty array.
   */
  readonly keywords = computed(() =>
    (this.keywordsResource.hasValue() ? this.keywordsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/keywords');

  protected convertValueFromServer(restKeyword: RestKeyword): IKeyword {
    return {
      ...restKeyword,
      createdAt: restKeyword.createdAt ? dayjs(restKeyword.createdAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class KeywordService extends KeywordsService {
  protected readonly http = inject(HttpClient);

  create(keyword: NewKeyword): Observable<IKeyword> {
    const copy = this.convertValueFromClient(keyword);
    return this.http.post<RestKeyword>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(keyword: IKeyword): Observable<IKeyword> {
    const copy = this.convertValueFromClient(keyword);
    return this.http
      .put<RestKeyword>(`${this.resourceUrl}/${encodeURIComponent(this.getKeywordIdentifier(keyword))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(keyword: PartialUpdateKeyword): Observable<IKeyword> {
    const copy = this.convertValueFromClient(keyword);
    return this.http
      .patch<RestKeyword>(`${this.resourceUrl}/${encodeURIComponent(this.getKeywordIdentifier(keyword))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IKeyword> {
    return this.http
      .get<RestKeyword>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IKeyword[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestKeyword[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getKeywordIdentifier(keyword: Pick<IKeyword, 'id'>): number {
    return keyword.id;
  }

  compareKeyword(o1: Pick<IKeyword, 'id'> | null, o2: Pick<IKeyword, 'id'> | null): boolean {
    return o1 && o2 ? this.getKeywordIdentifier(o1) === this.getKeywordIdentifier(o2) : o1 === o2;
  }

  addKeywordToCollectionIfMissing<Type extends Pick<IKeyword, 'id'>>(
    keywordCollection: Type[],
    ...keywordsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const keywords: Type[] = keywordsToCheck.filter(isPresent);
    if (keywords.length > 0) {
      const keywordCollectionIdentifiers = keywordCollection.map(keywordItem => this.getKeywordIdentifier(keywordItem));
      const keywordsToAdd = keywords.filter(keywordItem => {
        const keywordIdentifier = this.getKeywordIdentifier(keywordItem);
        if (keywordCollectionIdentifiers.includes(keywordIdentifier)) {
          return false;
        }
        keywordCollectionIdentifiers.push(keywordIdentifier);
        return true;
      });
      return [...keywordsToAdd, ...keywordCollection];
    }
    return keywordCollection;
  }

  protected convertValueFromClient<T extends IKeyword | NewKeyword | PartialUpdateKeyword>(keyword: T): RestOf<T> {
    return {
      ...keyword,
      createdAt: keyword.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestKeyword): IKeyword {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestKeyword[]): IKeyword[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
