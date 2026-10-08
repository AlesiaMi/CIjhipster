import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import CollectionRunResolve from './route/collection-run-routing-resolve.service';

const collectionRunRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/collection-run').then(m => m.CollectionRun),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/collection-run-detail').then(m => m.CollectionRunDetail),
    resolve: {
      collectionRun: CollectionRunResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/collection-run-update').then(m => m.CollectionRunUpdate),
    resolve: {
      collectionRun: CollectionRunResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/collection-run-update').then(m => m.CollectionRunUpdate),
    resolve: {
      collectionRun: CollectionRunResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default collectionRunRoute;
