import { Routes } from '@angular/router';

import { ASC } from 'app/config';
import { userRouteAccessService } from 'app/core/auth';

import DataSourceResolve from './route/data-source-routing-resolve.service';

const dataSourceRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/data-source').then(m => m.DataSource),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/data-source-detail').then(m => m.DataSourceDetail),
    resolve: {
      dataSource: DataSourceResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/data-source-update').then(m => m.DataSourceUpdate),
    resolve: {
      dataSource: DataSourceResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/data-source-update').then(m => m.DataSourceUpdate),
    resolve: {
      dataSource: DataSourceResolve,
    },
    canActivate: [userRouteAccessService],
  },
];

export default dataSourceRoute;
