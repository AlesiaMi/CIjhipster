import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import AnalysisResultResolve from './route/analysis-result-routing-resolve.service';

const analysisResultRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/analysis-result').then(m => m.AnalysisResult),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/analysis-result-detail').then(m => m.AnalysisResultDetail),
    resolve: {
      analysisResult: AnalysisResultResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/analysis-result-update').then(m => m.AnalysisResultUpdate),
    resolve: {
      analysisResult: AnalysisResultResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/analysis-result-update').then(m => m.AnalysisResultUpdate),
    resolve: {
      analysisResult: AnalysisResultResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default analysisResultRoute;
