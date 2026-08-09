import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IAnalysisResult } from '../analysis-result.model';
import { AnalysisResultService } from '../service/analysis-result.service';

const analysisResultResolve = (route: ActivatedRouteSnapshot): Observable<null | IAnalysisResult> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(AnalysisResultService);
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

export default analysisResultResolve;
