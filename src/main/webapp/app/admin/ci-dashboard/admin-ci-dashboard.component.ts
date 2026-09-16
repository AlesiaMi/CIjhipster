import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { finalize } from 'rxjs';

interface LastRun {
  id: number;
  status: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  foundCount: number;
  processedCount: number;
  errorMessage: string | null;
}

interface AdminUserSummary {
  id: number;
  login: string;
  email: string | null;
  activated: boolean;
  createdDate: string | null;

  competitorsCount: number;
  sourcesCount: number;
  rssCount: number;
  newsCount: number;
  analysisCount: number;

  lastRun: LastRun | null;
}

interface AdminDashboard {
  usersCount: number;
  competitorsCount: number;
  sourcesCount: number;
  rssCount: number;
  newsCount: number;
  analysisCount: number;
  collectionRunsCount: number;

  users: AdminUserSummary[];
}

interface AdminCompetitor {
  id: number;
  name: string;
}

interface AdminSource {
  id: number;
  name: string;
  url: string;
  sourceType: string | null;
  active: boolean;
  competitorId: number | null;
  competitorName: string | null;
}

interface AdminRun {
  id: number;
  status: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  foundCount: number;
  processedCount: number;
  errorMessage: string | null;
}

interface AdminUserDashboard {
  id: number;
  login: string;
  email: string | null;
  activated: boolean;
  createdDate: string | null;

  competitorsCount: number;
  sourcesCount: number;
  rssCount: number;
  newsCount: number;
  analysisCount: number;
  collectionRunsCount: number;

  competitors: AdminCompetitor[];
  sources: AdminSource[];
  recentRuns: AdminRun[];
}

@Component({
  standalone: true,
  selector: 'jhi-admin-ci-dashboard',
  imports: [CommonModule],
  templateUrl: './admin-ci-dashboard.component.html',
  styleUrls: ['./admin-ci-dashboard.component.scss'],
})
export class AdminCiDashboardComponent implements OnInit {
  private readonly http = inject(HttpClient);

  dashboard: AdminDashboard = {
    usersCount: 0,
    competitorsCount: 0,
    sourcesCount: 0,
    rssCount: 0,
    newsCount: 0,
    analysisCount: 0,
    collectionRunsCount: 0,
    users: [],
  };

  selectedUser: AdminUserDashboard | null = null;

  loading = true;
  userLoading = false;

  errorMessage = '';
  userErrorMessage = '';

  searchText = '';

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http
      .get<AdminDashboard>('/api/admin/ci-dashboard')
      .pipe(
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe({
        next: data => {
          console.log('ADMIN DASHBOARD DATA:', data);
          this.dashboard = data;
        },
        error: error => {
          console.error('Failed to load admin dashboard', error);
          this.errorMessage = 'Не удалось загрузить административный dashboard.';
        },
      });
  }

  openUser(user: AdminUserSummary): void {
    this.userLoading = true;
    this.userErrorMessage = '';

    this.http
      .get<AdminUserDashboard>(`/api/admin/ci-dashboard/users/${user.id}`)
      .pipe(
        finalize(() => {
          this.userLoading = false;
        }),
      )
      .subscribe({
        next: data => {
          this.selectedUser = data;
        },
        error: error => {
          console.error('Failed to load user dashboard', error);
          this.userErrorMessage = 'Не удалось загрузить данные пользователя.';
        },
      });
  }

  closeUser(): void {
    this.selectedUser = null;
    this.userErrorMessage = '';
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchText = input.value;
  }

  get filteredUsers(): AdminUserSummary[] {
    const value = this.searchText.trim().toLowerCase();

    if (!value) {
      return this.dashboard.users ?? [];
    }

    return (this.dashboard.users ?? []).filter(user => {
      const login = user.login?.toLowerCase() ?? '';
      const email = user.email?.toLowerCase() ?? '';

      return login.includes(value) || email.includes(value);
    });
  }

  runStatusClass(status: string | null | undefined): string {
    switch (status) {
      case 'SUCCESS':
        return 'status-success';

      case 'FAILED':
        return 'status-failed';

      case 'RUNNING':
        return 'status-running';

      default:
        return 'status-unknown';
    }
  }

  runStatusLabel(status: string | null | undefined): string {
    switch (status) {
      case 'SUCCESS':
        return 'Успешно';

      case 'FAILED':
        return 'Ошибка';

      case 'RUNNING':
        return 'Выполняется';

      default:
        return 'Нет запусков';
    }
  }

  sourceStatusClass(active: boolean): string {
    return active ? 'source-active' : 'source-disabled';
  }

  sourceStatusLabel(active: boolean): string {
    return active ? 'Активен' : 'Выключен';
  }

  refresh(): void {
    this.loadDashboard();

    if (this.selectedUser) {
      const user = this.dashboard.users.find(item => item.id === this.selectedUser?.id);

      if (user) {
        this.openUser(user);
      }
    }
  }
}
