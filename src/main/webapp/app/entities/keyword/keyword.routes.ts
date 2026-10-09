import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import KeywordResolve from './route/keyword-routing-resolve.service';

const keywordRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/keyword').then(m => m.Keyword),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/keyword-detail').then(m => m.KeywordDetail),
    resolve: {
      keyword: KeywordResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/keyword-update').then(m => m.KeywordUpdate),
    resolve: {
      keyword: KeywordResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/keyword-update').then(m => m.KeywordUpdate),
    resolve: {
      keyword: KeywordResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default keywordRoute;
