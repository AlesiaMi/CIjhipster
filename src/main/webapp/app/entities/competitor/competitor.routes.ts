import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { CompetitorEditRouteAccessService } from 'app/core/manager/manager-permission-route-access.service';
import CompetitorResolve from './route/competitor-routing-resolve.service';

const competitorRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/competitor').then(m => m.Competitor),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/competitor-detail').then(m => m.CompetitorDetail),
    resolve: {
      competitor: CompetitorResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/competitor-update').then(m => m.CompetitorUpdate),
    resolve: {
      competitor: CompetitorResolve,
    },
    canActivate: [UserRouteAccessService, CompetitorEditRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/competitor-update').then(m => m.CompetitorUpdate),
    resolve: {
      competitor: CompetitorResolve,
    },
    canActivate: [UserRouteAccessService, CompetitorEditRouteAccessService],
  },
];

export default competitorRoute;
