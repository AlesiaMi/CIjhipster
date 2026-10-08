import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import AnalysisResultResolve from './route/analysis-result-routing-resolve.service';

const analysisResultRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/analysis-result').then(m => m.AnalysisResult),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/analysis-result-detail').then(m => m.AnalysisResultDetail),
    resolve: {
      analysisResult: AnalysisResultResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/analysis-result-update').then(m => m.AnalysisResultUpdate),
    resolve: {
      analysisResult: AnalysisResultResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/analysis-result-update').then(m => m.AnalysisResultUpdate),
    resolve: {
      analysisResult: AnalysisResultResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default analysisResultRoute;
