import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  standalone: true,
  selector: 'jhi-client-dashboard',
  imports: [CommonModule],
  templateUrl: './client-dashboard.component.html',
  styleUrls: ['./client-dashboard.component.scss'],
})
export class ClientDashboardComponent implements OnInit {
  dashboard: any = {
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

  latestNewsItems: any[] = [];
  collectionRuns: any[] = [];

  loading = true;

  private http = inject(HttpClient);

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;

    this.http.get<any>('/api/dashboard').subscribe({
      next: data => {
        this.dashboard = data;
      },
      error: () => {
        this.loading = false;
      },
    });

    this.http.get<any[]>('/api/dashboard/latest-news').subscribe(data => {
      this.latestNewsItems = data;
    });

    this.http.get<any[]>('/api/collection-runs?size=1&sort=startedAt,desc').subscribe({
      next: data => {
        this.collectionRuns = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  // KPI

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
    return this.dashboard.sourceCount ?? 0;
  }

  getCompetitorsCount(): number {
    return this.dashboard.competitorCount ?? 0;
  }

  // Последние записи

  getLatestNews(): any[] {
    return this.latestNewsItems;
  }

  getLatestAnalysis(): any[] {
    return this.dashboard.latestAnalysis ?? [];
  }

  getLatestAlerts(): any[] {
    return this.dashboard.latestAlerts ?? [];
  }

  getLastRun(): any {
    return this.collectionRuns.length ? this.collectionRuns[0] : null;
  }

  // Проценты для диаграммы

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
  // Цвет тональности

  sentimentClass(sentiment: string): string {
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

  // Цвет Alert

  severityClass(severity: string): string {
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

  refresh(): void {
    this.loadDashboard();
  }
}
