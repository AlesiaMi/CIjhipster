import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IDataSource } from '../data-source.model';
import { DataSourceService } from '../service/data-source.service';

const dataSourceResolve = (route: ActivatedRouteSnapshot): Observable<null | IDataSource> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(DataSourceService);
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

export default dataSourceResolve;
