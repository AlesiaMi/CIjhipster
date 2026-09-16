import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';

import { ICompetitor, NewCompetitor } from '../competitor.model';

export type PartialUpdateCompetitor = Partial<ICompetitor> & Pick<ICompetitor, 'id'>;

@Injectable()
export class CompetitorsService {
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected readonly managerClientContext = inject(ManagerClientContextService);

  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/competitors');

  readonly competitorsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );

  readonly competitorsResource = httpResource<ICompetitor[]>(() => {
    const params = this.competitorsParams();

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

  readonly competitors = computed(() => (this.competitorsResource.hasValue() ? this.competitorsResource.value() : []));
}

@Injectable({ providedIn: 'root' })
export class CompetitorService extends CompetitorsService {
  protected readonly http = inject(HttpClient);

  create(competitor: NewCompetitor): Observable<ICompetitor> {
    const params = createRequestOption(this.managerClientContext.withClientUserId());

    return this.http.post<ICompetitor>(this.resourceUrl, competitor, { params });
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
    const options = createRequestOption(this.managerClientContext.withClientUserId(req ?? {}));

    return this.http.get<ICompetitor[]>(this.resourceUrl, {
      params: options,
      observe: 'response',
    });
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
    const competitors: Type[] = competitorsToCheck.filter(isPresent);

    if (competitors.length > 0) {
      const identifiers = competitorCollection.map(item => this.getCompetitorIdentifier(item));

      const competitorsToAdd = competitors.filter(item => {
        const identifier = this.getCompetitorIdentifier(item);

        if (identifiers.includes(identifier)) {
          return false;
        }

        identifiers.push(identifier);

        return true;
      });

      return [...competitorsToAdd, ...competitorCollection];
    }

    return competitorCollection;
  }
}
