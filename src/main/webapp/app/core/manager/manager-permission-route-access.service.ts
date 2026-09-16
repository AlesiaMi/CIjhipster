import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { catchError, map, of } from 'rxjs';

import { ManagerClientContextService, ManagerPermission } from './manager-client-context.service';

const createManagerPermissionGuard = (permission: ManagerPermission): CanActivateFn => {
  return () => {
    const managerClientContext = inject(ManagerClientContextService);

    const router = inject(Router);

    return managerClientContext.initialize().pipe(
      map(() => {
        // Обычный USER и ADMIN проходят.
        // Их права дальше проверяет backend.
        if (!managerClientContext.isManager()) {
          return true;
        }

        // Для MANAGER проверяем permission
        // именно выбранного клиента.
        if (managerClientContext.hasPermission(permission)) {
          return true;
        }

        return router.createUrlTree(['/accessdenied']);
      }),

      catchError(() => of(router.createUrlTree(['/accessdenied']))),
    );
  };
};

export const CompetitorEditRouteAccessService = createManagerPermissionGuard('COMPETITORS_EDIT');

export const DataSourceEditRouteAccessService = createManagerPermissionGuard('SOURCES_EDIT');
