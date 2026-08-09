import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { ICiAlert } from '../ci-alert.model';
import { CiAlertService } from '../service/ci-alert.service';

const ciAlertResolve = (route: ActivatedRouteSnapshot): Observable<null | ICiAlert> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(CiAlertService);
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

export default ciAlertResolve;
