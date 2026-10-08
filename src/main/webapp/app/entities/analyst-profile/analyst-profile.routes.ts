import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import AnalystProfileResolve from './route/analyst-profile-routing-resolve.service';

const analystProfileRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/analyst-profile').then(m => m.AnalystProfile),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/analyst-profile-detail').then(m => m.AnalystProfileDetail),
    resolve: {
      analystProfile: AnalystProfileResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/analyst-profile-update').then(m => m.AnalystProfileUpdate),
    resolve: {
      analystProfile: AnalystProfileResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/analyst-profile-update').then(m => m.AnalystProfileUpdate),
    resolve: {
      analystProfile: AnalystProfileResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default analystProfileRoute;
