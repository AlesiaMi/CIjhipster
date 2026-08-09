import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import AnalystProfileResolve from './route/analyst-profile-routing-resolve.service';

const analystProfileRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/analyst-profile').then(m => m.AnalystProfile),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/analyst-profile-detail').then(m => m.AnalystProfileDetail),
    resolve: {
      analystProfile: AnalystProfileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/analyst-profile-update').then(m => m.AnalystProfileUpdate),
    resolve: {
      analystProfile: AnalystProfileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/analyst-profile-update').then(m => m.AnalystProfileUpdate),
    resolve: {
      analystProfile: AnalystProfileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default analystProfileRoute;
