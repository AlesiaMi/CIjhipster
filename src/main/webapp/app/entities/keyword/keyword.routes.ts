import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import KeywordResolve from './route/keyword-routing-resolve.service';

const keywordRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/keyword').then(m => m.Keyword),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/keyword-detail').then(m => m.KeywordDetail),
    resolve: {
      keyword: KeywordResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/keyword-update').then(m => m.KeywordUpdate),
    resolve: {
      keyword: KeywordResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/keyword-update').then(m => m.KeywordUpdate),
    resolve: {
      keyword: KeywordResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default keywordRoute;
