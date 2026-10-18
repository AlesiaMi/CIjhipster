import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ManagerClient, ManagerClientContextService } from 'app/core/manager/manager-client-context.service';

interface DashboardNewsItem {
  id: number;
  publishedAt?: string | null;
  title?: string | null;
  url?: string | null;
  competitorName?: string | null;
  dataSource?: {
    sourceName?: string | null;
  } | null;
}

interface DashboardAnalysis {
  id: number;
  topic?: string | null;
  sentiment?: string | null;
  summary?: string | null;
  modelName?: string | null;
}

interface DashboardAlert {
  id: number;
  title?: string | null;
  severity?: string | null;
  message?: string | null;
  status?: string | null;
}

interface CollectionRunSummary {
  id: number;
  status?: string | null;
  foundCount?: number | null;
  processedCount?: number | null;
  finishedAt?: string | null;
}

interface DashboardData {
  newsCount?: number;
  analysisCount?: number;
  competitorCount?: number;
  alertsCount?: number;
  highAlertsCount?: number;
  sourcesCount?: number;

  positiveCount?: number;
  negativeCount?: number;
  neutralCount?: number;
  unknownCount?: number;

  latestAnalysis?: DashboardAnalysis[];
  latestAlerts?: DashboardAlert[];
}

function createEmptyDashboard(): DashboardData {
  return {
    newsCount: 0,
    analysisCount: 0,
    competitorCount: 0,
    alertsCount: 0,
    highAlertsCount: 0,
    sourcesCount: 0,

    positiveCount: 0,
    negativeCount: 0,
    neutralCount: 0,
    unknownCount: 0,

    latestAnalysis: [],
    latestAlerts: [],
  };
}

@Component({
  standalone: true,
  selector: 'jhi-client-dashboard',
  imports: [CommonModule, FormsModule],
  templateUrl: './client-dashboard.component.html',
  styleUrls: ['./client-dashboard.component.scss'],
})
export class ClientDashboardComponent implements OnInit {
  dashboard: DashboardData = createEmptyDashboard();

  latestNewsItems: DashboardNewsItem[] = [];
  collectionRuns: CollectionRunSummary[] = [];

  loading = true;

  collectionRunning = false;
  collectionMessage = '';

  isManager = false;
  managerClients: ManagerClient[] = [];
  selectedClientUserId: number | null = null;
  clientsLoading = false;
  clientMessage = '';

  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly managerClientContext = inject(ManagerClientContextService);

  ngOnInit(): void {
    this.loadManagerClients();
  }

  loadManagerClients(): void {
    this.clientsLoading = true;
    this.clientMessage = '';
    this.cdr.markForCheck();

    this.managerClientContext.initialize().subscribe({
      next: () => {
        this.isManager = this.managerClientContext.isManager();
        this.managerClients = this.managerClientContext.clients();
        this.selectedClientUserId = this.managerClientContext.selectedClientUserId();

        this.clientsLoading = false;

        if (this.isManager && this.selectedClientUserId === null) {
          this.clientMessage = 'Нет доступных клиентов.';
          this.loading = false;
          this.cdr.markForCheck();
          return;
        }

        this.cdr.markForCheck();
        this.loadDashboard();
      },

      error: () => {
        this.clientsLoading = false;
        this.loading = false;
        this.clientMessage = 'Не удалось загрузить список клиентов.';
        this.cdr.markForCheck();
      },
    });
  }

  onClientChange(): void {
    this.managerClientContext.selectClient(this.selectedClientUserId);
    this.collectionMessage = '';
    this.resetDashboard();

    if (this.selectedClientUserId !== null) {
      this.loadDashboard();
    } else {
      this.loading = false;
    }

    this.cdr.markForCheck();
  }

  loadDashboard(): void {
    if (this.isManager && this.selectedClientUserId === null) {
      this.loading = false;
      this.cdr.markForCheck();
      return;
    }

    this.loading = true;
    this.cdr.markForCheck();

    const dashboardParams = this.getClientParams();

    this.http.get<DashboardData>('/api/dashboard', { params: dashboardParams }).subscribe({
      next: data => {
        this.dashboard = data;
        this.cdr.markForCheck();
      },
      error: error => {
        console.error('Ошибка загрузки дашборда:', error);
        this.loading = false;
        this.cdr.markForCheck();
      },
    });

    this.http.get<DashboardNewsItem[]>('/api/dashboard/latest-news', { params: dashboardParams }).subscribe({
      next: data => {
        this.latestNewsItems = data;
        this.cdr.markForCheck();
      },
      error: error => {
        console.error('Ошибка загрузки последних новостей:', error);
        this.latestNewsItems = [];
        this.cdr.markForCheck();
      },
    });

    let collectionParams = new HttpParams().set('size', '1').set('sort', 'startedAt,desc');

    if (this.isManager && this.selectedClientUserId !== null) {
      collectionParams = collectionParams.set('clientUserId', this.selectedClientUserId.toString());
    }

    this.http.get<CollectionRunSummary[]>('/api/collection-runs', { params: collectionParams }).subscribe({
      next: data => {
        this.collectionRuns = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: error => {
        console.error('Ошибка загрузки истории сборов:', error);
        this.collectionRuns = [];
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  getSelectedClient(): ManagerClient | null {
    if (this.selectedClientUserId === null) {
      return null;
    }

    return this.managerClients.find(client => client.userId === this.selectedClientUserId) ?? null;
  }

  canRunCollection(): boolean {
    if (!this.isManager) {
      return true;
    }

    const client = this.getSelectedClient();
    return client?.permissions.includes('COLLECTION_RUN') ?? false;
  }

  runCollection(): void {
    if (this.collectionRunning || !this.canRunCollection()) {
      return;
    }

    this.collectionRunning = true;
    this.collectionMessage = '';
    this.cdr.markForCheck();

    const params = this.getClientParams();

    this.http.post<unknown>('/api/collection-job/run-rss', {}, { params }).subscribe({
      next: () => {
        this.collectionMessage = 'Сбор новостей запущен.';
        this.collectionRunning = false;
        this.cdr.markForCheck();

        setTimeout(() => {
          this.loadDashboard();
        }, 2000);
      },

      error: (error: HttpErrorResponse) => {
        if (error.status === 409) {
          this.collectionMessage = 'Сбор новостей уже выполняется.';
        } else if (error.status === 403) {
          this.collectionMessage = 'Нет разрешения на запуск сбора новостей.';
        } else {
          this.collectionMessage = 'Не удалось запустить сбор новостей.';
        }

        this.collectionRunning = false;
        this.cdr.markForCheck();
      },
    });
  }

  refresh(): void {
    this.loadDashboard();
  }

  getNewsCount(): number {
    return this.dashboard.newsCount ?? 0;
  }

  getAnalysisCount(): number {
    return this.dashboard.analysisCount ?? 0;
  }

  getAlertsCount(): number {
    return this.dashboard.alertsCount ?? 0;
  }

  getNegativeCount(): number {
    return this.dashboard.negativeCount ?? 0;
  }

  getPositiveCount(): number {
    return this.dashboard.positiveCount ?? 0;
  }

  getNeutralCount(): number {
    return this.dashboard.neutralCount ?? 0;
  }

  getHighAlertsCount(): number {
    return this.dashboard.highAlertsCount ?? 0;
  }

  getSourcesCount(): number {
    return this.dashboard.sourcesCount ?? 0;
  }

  getCompetitorsCount(): number {
    return this.dashboard.competitorCount ?? 0;
  }

  getLatestNews(): DashboardNewsItem[] {
    return this.latestNewsItems;
  }

  getLatestAnalysis(): DashboardAnalysis[] {
    return this.dashboard.latestAnalysis ?? [];
  }

  getLatestAlerts(): DashboardAlert[] {
    return this.dashboard.latestAlerts ?? [];
  }

  getLastRun(): CollectionRunSummary | null {
    return this.collectionRuns.at(0) ?? null;
  }

  getPositivePercent(): number {
    const total = this.getAnalysisCount();

    if (!total) {
      return 0;
    }

    return Math.round((this.getPositiveCount() / total) * 100);
  }

  getNeutralPercent(): number {
    const total = this.getAnalysisCount();

    if (!total) {
      return 0;
    }

    return Math.round((this.getNeutralCount() / total) * 100);
  }

  getNegativePercent(): number {
    const total = this.getAnalysisCount();

    if (!total) {
      return 0;
    }

    return Math.round((this.getNegativeCount() / total) * 100);
  }

  sentimentClass(sentiment: string | null | undefined): string {
    switch (sentiment) {
      case 'POSITIVE':
        return 'bg-success';

      case 'NEGATIVE':
        return 'bg-danger';

      case 'NEUTRAL':
        return 'bg-warning';

      default:
        return 'bg-secondary';
    }
  }

  severityClass(severity: string | null | undefined): string {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-danger';

      case 'HIGH':
        return 'bg-warning';

      case 'MEDIUM':
        return 'bg-info';

      default:
        return 'bg-secondary';
    }
  }

  private getClientParams(): HttpParams {
    let params = new HttpParams();

    if (this.isManager && this.selectedClientUserId !== null) {
      params = params.set('clientUserId', this.selectedClientUserId.toString());
    }

    return params;
  }

  private resetDashboard(): void {
    this.dashboard = createEmptyDashboard();
    this.latestNewsItems = [];
    this.collectionRuns = [];
  }
}
