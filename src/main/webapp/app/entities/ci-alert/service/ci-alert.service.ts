import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ICiAlert, NewCiAlert } from '../ci-alert.model';

export type PartialUpdateCiAlert = Partial<ICiAlert> & Pick<ICiAlert, 'id'>;

type RestOf<T extends ICiAlert | NewCiAlert> = Omit<T, 'createdAt' | 'readAt'> & {
  createdAt?: string | null;
  readAt?: string | null;
};

export type RestCiAlert = RestOf<ICiAlert>;

export type NewRestCiAlert = RestOf<NewCiAlert>;

export type PartialUpdateRestCiAlert = RestOf<PartialUpdateCiAlert>;

@Injectable()
export class CiAlertsService {
  readonly ciAlertsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly ciAlertsResource = httpResource<RestCiAlert[]>(() => {
    const params = this.ciAlertsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of ciAlert that have been fetched. It is updated when the ciAlertsResource emits a new value.
   * In case of error while fetching the ciAlerts, the signal is set to an empty array.
   */
  readonly ciAlerts = computed(() =>
    (this.ciAlertsResource.hasValue() ? this.ciAlertsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/ci-alerts');

  protected convertValueFromServer(restCiAlert: RestCiAlert): ICiAlert {
    return {
      ...restCiAlert,
      createdAt: restCiAlert.createdAt ? dayjs(restCiAlert.createdAt) : undefined,
      readAt: restCiAlert.readAt ? dayjs(restCiAlert.readAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class CiAlertService extends CiAlertsService {
  protected readonly http = inject(HttpClient);

  create(ciAlert: NewCiAlert): Observable<ICiAlert> {
    const copy = this.convertValueFromClient(ciAlert);
    return this.http.post<RestCiAlert>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(ciAlert: ICiAlert): Observable<ICiAlert> {
    const copy = this.convertValueFromClient(ciAlert);
    return this.http
      .put<RestCiAlert>(`${this.resourceUrl}/${encodeURIComponent(this.getCiAlertIdentifier(ciAlert))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(ciAlert: PartialUpdateCiAlert): Observable<ICiAlert> {
    const copy = this.convertValueFromClient(ciAlert);
    return this.http
      .patch<RestCiAlert>(`${this.resourceUrl}/${encodeURIComponent(this.getCiAlertIdentifier(ciAlert))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<ICiAlert> {
    return this.http
      .get<RestCiAlert>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<ICiAlert[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestCiAlert[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getCiAlertIdentifier(ciAlert: Pick<ICiAlert, 'id'>): number {
    return ciAlert.id;
  }

  compareCiAlert(o1: Pick<ICiAlert, 'id'> | null, o2: Pick<ICiAlert, 'id'> | null): boolean {
    return o1 && o2 ? this.getCiAlertIdentifier(o1) === this.getCiAlertIdentifier(o2) : o1 === o2;
  }

  addCiAlertToCollectionIfMissing<Type extends Pick<ICiAlert, 'id'>>(
    ciAlertCollection: Type[],
    ...ciAlertsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const ciAlerts: Type[] = ciAlertsToCheck.filter(isPresent);
    if (ciAlerts.length > 0) {
      const ciAlertCollectionIdentifiers = ciAlertCollection.map(ciAlertItem => this.getCiAlertIdentifier(ciAlertItem));
      const ciAlertsToAdd = ciAlerts.filter(ciAlertItem => {
        const ciAlertIdentifier = this.getCiAlertIdentifier(ciAlertItem);
        if (ciAlertCollectionIdentifiers.includes(ciAlertIdentifier)) {
          return false;
        }
        ciAlertCollectionIdentifiers.push(ciAlertIdentifier);
        return true;
      });
      return [...ciAlertsToAdd, ...ciAlertCollection];
    }
    return ciAlertCollection;
  }

  protected convertValueFromClient<T extends ICiAlert | NewCiAlert | PartialUpdateCiAlert>(ciAlert: T): RestOf<T> {
    return {
      ...ciAlert,
      createdAt: ciAlert.createdAt?.toJSON() ?? null,
      readAt: ciAlert.readAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestCiAlert): ICiAlert {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestCiAlert[]): ICiAlert[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
