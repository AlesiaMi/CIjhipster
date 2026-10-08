import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import CiAlertResolve from './route/ci-alert-routing-resolve.service';

const ciAlertRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/ci-alert').then(m => m.CiAlert),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/ci-alert-detail').then(m => m.CiAlertDetail),
    resolve: {
      ciAlert: CiAlertResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/ci-alert-update').then(m => m.CiAlertUpdate),
    resolve: {
      ciAlert: CiAlertResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/ci-alert-update').then(m => m.CiAlertUpdate),
    resolve: {
      ciAlert: CiAlertResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default ciAlertRoute;
