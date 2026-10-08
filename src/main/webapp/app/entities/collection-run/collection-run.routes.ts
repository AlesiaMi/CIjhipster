import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import CollectionRunResolve from './route/collection-run-routing-resolve.service';

const collectionRunRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/collection-run').then(m => m.CollectionRun),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/collection-run-detail').then(m => m.CollectionRunDetail),
    resolve: {
      collectionRun: CollectionRunResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/collection-run-update').then(m => m.CollectionRunUpdate),
    resolve: {
      collectionRun: CollectionRunResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/collection-run-update').then(m => m.CollectionRunUpdate),
    resolve: {
      collectionRun: CollectionRunResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default collectionRunRoute;
