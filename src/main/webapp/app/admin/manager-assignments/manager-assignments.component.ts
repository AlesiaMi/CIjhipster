import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { IUserManagement } from 'app/entities/admin/user-management/user-management.model';
import { UserManagementService } from 'app/entities/admin/user-management/service/user-management.service';

type ManagerPermission = 'VIEW' | 'COMPETITORS_EDIT' | 'SOURCES_EDIT' | 'COLLECTION_RUN' | 'NEWS_VIEW' | 'ANALYSIS_VIEW';

interface ManagerAssignment {
  id: number;
  managerId: number;
  managerLogin: string;
  clientUserId: number;
  clientLogin: string;
  active: boolean;
  createdAt: string;
  permissions: ManagerPermission[];
}

interface ManagerAssignmentRequest {
  managerId: number;
  clientUserId: number;
  active: boolean;
  permissions: ManagerPermission[];
}

interface EditableAssignment extends ManagerAssignment {
  saving?: boolean;
}

@Component({
  selector: 'jhi-manager-assignments',
  standalone: true,
  templateUrl: './manager-assignments.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, FontAwesomeModule],
})
export class ManagerAssignmentsComponent implements OnInit {
  private readonly http = inject(HttpClient);

  private readonly userManagementService = inject(UserManagementService);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly assignments = signal<EditableAssignment[]>([]);

  readonly managers = signal<IUserManagement[]>([]);

  readonly clients = signal<IUserManagement[]>([]);

  readonly permissionOptions: {
    value: ManagerPermission;
    label: string;
  }[] = [
    {
      value: 'VIEW',
      label: 'Просмотр данных клиента',
    },
    {
      value: 'COMPETITORS_EDIT',
      label: 'Редактирование конкурентов',
    },
    {
      value: 'SOURCES_EDIT',
      label: 'Редактирование источников',
    },
    {
      value: 'COLLECTION_RUN',
      label: 'Запуск сбора новостей',
    },
    {
      value: 'NEWS_VIEW',
      label: 'Просмотр новостей',
    },
    {
      value: 'ANALYSIS_VIEW',
      label: 'Просмотр результатов анализа',
    },
  ];

  newManagerId: number | null = null;
  newClientUserId: number | null = null;

  newPermissions: ManagerPermission[] = ['VIEW'];

  ngOnInit(): void {
    this.loadUsers();
    this.loadAssignments();
  }

  loadAssignments(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.http.get<ManagerAssignment[]>('/api/admin/manager-assignments').subscribe({
      next: assignments => {
        this.assignments.set(
          assignments.map(assignment => ({
            ...assignment,
            permissions: [...assignment.permissions],
            saving: false,
          })),
        );

        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Не удалось загрузить назначения менеджеров.');
        this.loading.set(false);
      },
    });
  }

  loadUsers(): void {
    this.userManagementService
      .query({
        page: 0,
        size: 1000,
        sort: ['login,asc'],
      })
      .subscribe({
        next: response => {
          const users = response.body ?? [];

          this.managers.set(users.filter(user => user.authorities?.includes('ROLE_MANAGER')));

          this.clients.set(users.filter(user => user.authorities?.includes('ROLE_USER')));
        },
        error: () => {
          this.errorMessage.set('Не удалось загрузить пользователей.');
        },
      });
  }

  createAssignment(): void {
    this.clearMessages();

    if (this.newManagerId === null || this.newClientUserId === null) {
      this.errorMessage.set('Выберите менеджера и клиента.');
      return;
    }

    const request: ManagerAssignmentRequest = {
      managerId: this.newManagerId,
      clientUserId: this.newClientUserId,
      active: true,
      permissions: [...this.newPermissions],
    };

    this.http.post<ManagerAssignment>('/api/admin/manager-assignments', request).subscribe({
      next: () => {
        this.successMessage.set('Назначение создано.');

        this.newManagerId = null;
        this.newClientUserId = null;
        this.newPermissions = ['VIEW'];

        this.loadAssignments();
      },
      error: error => {
        this.errorMessage.set(error?.error?.detail ?? 'Не удалось создать назначение.');
      },
    });
  }

  saveAssignment(assignment: EditableAssignment): void {
    this.clearMessages();

    assignment.saving = true;

    const request: ManagerAssignmentRequest = {
      managerId: assignment.managerId,
      clientUserId: assignment.clientUserId,
      active: assignment.active,
      permissions: [...assignment.permissions],
    };

    this.http.put<ManagerAssignment>(`/api/admin/manager-assignments/${assignment.id}`, request).subscribe({
      next: updated => {
        assignment.active = updated.active;
        assignment.permissions = [...updated.permissions];
        assignment.saving = false;

        this.successMessage.set(`Назначение ${updated.managerLogin} → ${updated.clientLogin} сохранено.`);
      },
      error: error => {
        assignment.saving = false;

        this.errorMessage.set(error?.error?.detail ?? 'Не удалось сохранить назначение.');
      },
    });
  }

  hasPermission(permissions: ManagerPermission[], permission: ManagerPermission): boolean {
    return permissions.includes(permission);
  }

  setExistingPermission(assignment: EditableAssignment, permission: ManagerPermission, checked: boolean): void {
    if (checked) {
      if (!assignment.permissions.includes(permission)) {
        assignment.permissions = [...assignment.permissions, permission];
      }

      return;
    }

    assignment.permissions = assignment.permissions.filter(existing => existing !== permission);
  }

  hasNewPermission(permission: ManagerPermission): boolean {
    return this.newPermissions.includes(permission);
  }

  setNewPermission(permission: ManagerPermission, checked: boolean): void {
    if (checked) {
      if (!this.newPermissions.includes(permission)) {
        this.newPermissions = [...this.newPermissions, permission];
      }

      return;
    }

    this.newPermissions = this.newPermissions.filter(existing => existing !== permission);
  }

  private clearMessages(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }
}
