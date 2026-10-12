import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { userRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Authority } from 'app/shared/jhipster/constants';
import NewsItemResolve from './route/news-item-routing-resolve.service';

const newsItemRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/news-item').then(m => m.NewsItem),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/news-item-detail').then(m => m.NewsItemDetail),
    resolve: {
      newsItem: NewsItemResolve,
    },
    canActivate: [userRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/news-item-update').then(m => m.NewsItemUpdate),
    resolve: {
      newsItem: NewsItemResolve,
    },
    data: { authorities: [Authority.ADMIN] },
    canActivate: [userRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/news-item-update').then(m => m.NewsItemUpdate),
    resolve: {
      newsItem: NewsItemResolve,
    },
    data: { authorities: [Authority.ADMIN] },
    canActivate: [userRouteAccessService],
  },
];

export default newsItemRoute;
