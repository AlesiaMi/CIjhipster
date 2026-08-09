import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IAnalystProfile, NewAnalystProfile } from '../analyst-profile.model';

export type PartialUpdateAnalystProfile = Partial<IAnalystProfile> & Pick<IAnalystProfile, 'id'>;

type RestOf<T extends IAnalystProfile | NewAnalystProfile> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestAnalystProfile = RestOf<IAnalystProfile>;

export type NewRestAnalystProfile = RestOf<NewAnalystProfile>;

export type PartialUpdateRestAnalystProfile = RestOf<PartialUpdateAnalystProfile>;

@Injectable()
export class AnalystProfilesService {
  readonly analystProfilesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly analystProfilesResource = httpResource<RestAnalystProfile[]>(() => {
    const params = this.analystProfilesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of analystProfile that have been fetched. It is updated when the analystProfilesResource emits a new value.
   * In case of error while fetching the analystProfiles, the signal is set to an empty array.
   */
  readonly analystProfiles = computed(() =>
    (this.analystProfilesResource.hasValue() ? this.analystProfilesResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/analyst-profiles');

  protected convertValueFromServer(restAnalystProfile: RestAnalystProfile): IAnalystProfile {
    return {
      ...restAnalystProfile,
      createdAt: restAnalystProfile.createdAt ? dayjs(restAnalystProfile.createdAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class AnalystProfileService extends AnalystProfilesService {
  protected readonly http = inject(HttpClient);

  create(analystProfile: NewAnalystProfile): Observable<IAnalystProfile> {
    const copy = this.convertValueFromClient(analystProfile);
    return this.http.post<RestAnalystProfile>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(analystProfile: IAnalystProfile): Observable<IAnalystProfile> {
    const copy = this.convertValueFromClient(analystProfile);
    return this.http
      .put<RestAnalystProfile>(`${this.resourceUrl}/${encodeURIComponent(this.getAnalystProfileIdentifier(analystProfile))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(analystProfile: PartialUpdateAnalystProfile): Observable<IAnalystProfile> {
    const copy = this.convertValueFromClient(analystProfile);
    return this.http
      .patch<RestAnalystProfile>(`${this.resourceUrl}/${encodeURIComponent(this.getAnalystProfileIdentifier(analystProfile))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IAnalystProfile> {
    return this.http
      .get<RestAnalystProfile>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IAnalystProfile[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAnalystProfile[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getAnalystProfileIdentifier(analystProfile: Pick<IAnalystProfile, 'id'>): number {
    return analystProfile.id;
  }

  compareAnalystProfile(o1: Pick<IAnalystProfile, 'id'> | null, o2: Pick<IAnalystProfile, 'id'> | null): boolean {
    return o1 && o2 ? this.getAnalystProfileIdentifier(o1) === this.getAnalystProfileIdentifier(o2) : o1 === o2;
  }

  addAnalystProfileToCollectionIfMissing<Type extends Pick<IAnalystProfile, 'id'>>(
    analystProfileCollection: Type[],
    ...analystProfilesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const analystProfiles: Type[] = analystProfilesToCheck.filter(isPresent);
    if (analystProfiles.length > 0) {
      const analystProfileCollectionIdentifiers = analystProfileCollection.map(analystProfileItem =>
        this.getAnalystProfileIdentifier(analystProfileItem),
      );
      const analystProfilesToAdd = analystProfiles.filter(analystProfileItem => {
        const analystProfileIdentifier = this.getAnalystProfileIdentifier(analystProfileItem);
        if (analystProfileCollectionIdentifiers.includes(analystProfileIdentifier)) {
          return false;
        }
        analystProfileCollectionIdentifiers.push(analystProfileIdentifier);
        return true;
      });
      return [...analystProfilesToAdd, ...analystProfileCollection];
    }
    return analystProfileCollection;
  }

  protected convertValueFromClient<T extends IAnalystProfile | NewAnalystProfile | PartialUpdateAnalystProfile>(
    analystProfile: T,
  ): RestOf<T> {
    return {
      ...analystProfile,
      createdAt: analystProfile.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestAnalystProfile): IAnalystProfile {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestAnalystProfile[]): IAnalystProfile[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
