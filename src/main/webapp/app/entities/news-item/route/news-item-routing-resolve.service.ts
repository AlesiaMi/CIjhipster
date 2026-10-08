import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { INewsItem } from '../news-item.model';
import { NewsItemService } from '../service/news-item.service';

const newsItemResolve = (route: ActivatedRouteSnapshot): Observable<null | INewsItem> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(NewsItemService);
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

export default newsItemResolve;
