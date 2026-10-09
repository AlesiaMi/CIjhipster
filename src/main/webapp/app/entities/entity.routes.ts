import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'user-management',
    title: 'userManagement.home.title',
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  {
    path: 'authority',
    title: 'cIjhipsterApp.adminAuthority.home.title',
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'user-management',
    data: { pageTitle: 'userManagement.home.title' },
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  {
    path: 'competitor',
    title: 'cIjhipsterApp.competitor.home.title',
    loadChildren: () => import('./competitor/competitor.routes'),
  },
  {
    path: 'data-source',
    title: 'cIjhipsterApp.dataSource.home.title',
    loadChildren: () => import('./data-source/data-source.routes'),
  },
  {
    path: 'keyword',
    title: 'cIjhipsterApp.keyword.home.title',
    loadChildren: () => import('./keyword/keyword.routes'),
  },
  {
    path: 'collection-run',
    title: 'cIjhipsterApp.collectionRun.home.title',
    loadChildren: () => import('./collection-run/collection-run.routes'),
  },
  {
    path: 'news-item',
    title: 'cIjhipsterApp.newsItem.home.title',
    loadChildren: () => import('./news-item/news-item.routes'),
  },
  {
    path: 'analysis-result',
    title: 'cIjhipsterApp.analysisResult.home.title',
    loadChildren: () => import('./analysis-result/analysis-result.routes'),
  },
  {
    path: 'ci-alert',
    title: 'cIjhipsterApp.ciAlert.home.title',
    loadChildren: () => import('./ci-alert/ci-alert.routes'),
  },
  {
    path: 'analyst-profile',
    title: 'cIjhipsterApp.analystProfile.home.title',
    loadChildren: () => import('./analyst-profile/analyst-profile.routes'),
  },
  // jhipster-needle-add-entity-route - JHipster will add entity modules routes here
];

export default routes;
