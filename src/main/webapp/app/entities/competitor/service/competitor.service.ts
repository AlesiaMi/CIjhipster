import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { serverApiUrl } from 'app/config';
import { createRequestOption } from 'app/core/request';
import { ICompetitor, NewCompetitor } from '../competitor.model';

export type PartialUpdateCompetitor = Partial<ICompetitor> & Pick<ICompetitor, 'id'>;

@Service()
export class CompetitorsService {
  readonly competitorsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly competitorsResource = httpResource<ICompetitor[]>(() => {
    const params = this.competitorsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of competitor that have been fetched. It is updated when the competitorsResource emits a new value.
   * In case of error while fetching the competitors, the signal is set to an empty array.
   */
  readonly competitors = computed(() => (this.competitorsResource.hasValue() ? this.competitorsResource.value() : []));
  protected readonly resourceUrl = `${serverApiUrl}api/competitors`;
}

@Service()
export class CompetitorService extends CompetitorsService {
  protected readonly http = inject(HttpClient);

  create(competitor: NewCompetitor): Observable<ICompetitor> {
    return this.http.post<ICompetitor>(this.resourceUrl, competitor);
  }

  update(competitor: ICompetitor): Observable<ICompetitor> {
    return this.http.put<ICompetitor>(`${this.resourceUrl}/${encodeURIComponent(this.getCompetitorIdentifier(competitor))}`, competitor);
  }

  partialUpdate(competitor: PartialUpdateCompetitor): Observable<ICompetitor> {
    return this.http.patch<ICompetitor>(`${this.resourceUrl}/${encodeURIComponent(this.getCompetitorIdentifier(competitor))}`, competitor);
  }

  find(id: number): Observable<ICompetitor> {
    return this.http.get<ICompetitor>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<ICompetitor[]>> {
    const options = createRequestOption(req);
    return this.http.get<ICompetitor[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getCompetitorIdentifier(competitor: Pick<ICompetitor, 'id'>): number {
    return competitor.id;
  }

  compareCompetitor(o1: Pick<ICompetitor, 'id'> | null, o2: Pick<ICompetitor, 'id'> | null): boolean {
    return o1 && o2 ? this.getCompetitorIdentifier(o1) === this.getCompetitorIdentifier(o2) : o1 === o2;
  }

  addCompetitorToCollectionIfMissing<Type extends Pick<ICompetitor, 'id'>>(
    competitorCollection: Type[],
    ...competitorsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const competitors: Type[] = competitorsToCheck.filter(competitorItem => competitorItem !== null && competitorItem !== undefined);
    if (competitors.length > 0) {
      const competitorCollectionIdentifiers = competitorCollection.map(competitorItem => this.getCompetitorIdentifier(competitorItem));
      const competitorsToAdd = competitors.filter(competitorItem => {
        const competitorIdentifier = this.getCompetitorIdentifier(competitorItem);
        if (competitorCollectionIdentifiers.includes(competitorIdentifier)) {
          return false;
        }
        competitorCollectionIdentifiers.push(competitorIdentifier);
        return true;
      });
      return [...competitorsToAdd, ...competitorCollection];
    }
    return competitorCollection;
  }
}
