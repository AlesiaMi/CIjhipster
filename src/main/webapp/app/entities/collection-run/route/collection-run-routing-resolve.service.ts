import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { ICollectionRun } from '../collection-run.model';
import { CollectionRunService } from '../service/collection-run.service';

const collectionRunResolve = (route: ActivatedRouteSnapshot): Observable<null | ICollectionRun> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(CollectionRunService);
    return service.find(id).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          router.navigate(['404']);
        } else {
          router.navigate(['error']);
        }
        return EMPTY;
      }),
    );
  }

  return of(null);
};

export default collectionRunResolve;
