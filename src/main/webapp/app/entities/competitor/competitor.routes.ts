import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';
import { CompetitorEditRouteAccessService } from 'app/core/manager/manager-permission-route-access.service';
import CompetitorResolve from './route/competitor-routing-resolve.service';

const competitorRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/competitor').then(m => m.Competitor),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/competitor-detail').then(m => m.CompetitorDetail),
    resolve: {
      competitor: CompetitorResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/competitor-update').then(m => m.CompetitorUpdate),
    resolve: {
      competitor: CompetitorResolve,
    },
    canActivate: [userRouteAccessService, CompetitorEditRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/competitor-update').then(m => m.CompetitorUpdate),
    resolve: {
      competitor: CompetitorResolve,
    },
    canActivate: [userRouteAccessService, CompetitorEditRouteAccessService],
  },
];

export default competitorRoute;
