import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import CiAlertResolve from './route/ci-alert-routing-resolve.service';

const ciAlertRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/ci-alert').then(m => m.CiAlert),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/ci-alert-detail').then(m => m.CiAlertDetail),
    resolve: {
      ciAlert: CiAlertResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/ci-alert-update').then(m => m.CiAlertUpdate),
    resolve: {
      ciAlert: CiAlertResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/ci-alert-update').then(m => m.CiAlertUpdate),
    resolve: {
      ciAlert: CiAlertResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default ciAlertRoute;
