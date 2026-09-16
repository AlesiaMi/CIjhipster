import { Routes } from '@angular/router';
/* jhipster-needle-add-admin-module-import - JHipster will add admin modules imports here */

const routes: Routes = [
  {
    path: 'docs',
    loadComponent: () => import('./docs/docs'),
    title: 'global.menu.admin.apidocs',
  },
  {
    path: 'configuration',
    loadComponent: () => import('./configuration/configuration'),
    title: 'configuration.title',
  },
  {
    path: 'health',
    loadComponent: () => import('./health/health'),
    title: 'health.title',
  },
  {
    path: 'logs',
    loadComponent: () => import('./logs/logs'),
    title: 'logs.title',
  },
  {
    path: 'metrics',
    loadComponent: () => import('./metrics/metrics'),
    title: 'metrics.title',
  },
  {
    path: 'ci-dashboard',
    loadComponent: () => import('./ci-dashboard/admin-ci-dashboard.component').then(m => m.AdminCiDashboardComponent),
    title: 'Admin CI Dashboard',
  },
  {
    path: 'manager-assignments',
    loadComponent: () => import('./manager-assignments/manager-assignments.component').then(m => m.ManagerAssignmentsComponent),
    title: 'Manager assignments',
  },
  /* jhipster-needle-add-admin-route - JHipster will add admin routes here */
];

export default routes;
