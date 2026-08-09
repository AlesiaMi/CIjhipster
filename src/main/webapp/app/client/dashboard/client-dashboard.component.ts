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
  private http = inject(HttpClient);

  newsItems: any[] = [];
  analysisResults: any[] = [];
  alerts: any[] = [];
  collectionRuns: any[] = [];

  loading = true;

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;

    this.http.get<any[]>('/api/news-items?size=1000').subscribe(data => {
      this.newsItems = data;
    });

    this.http.get<any[]>('/api/analysis-results?size=1000').subscribe(data => {
      this.analysisResults = data;
    });

    this.http.get<any[]>('/api/ci-alerts?size=1000').subscribe(data => {
      this.alerts = data;
    });

    this.http.get<any[]>('/api/collection-runs?size=100').subscribe({
      next: data => {
        this.collectionRuns = data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  // =======================
  // KPI
  // =======================

  getNewsCount(): number {
    return this.newsItems.length;
  }

  getAnalysisCount(): number {
    return this.analysisResults.length;
  }

  getAlertsCount(): number {
    return this.alerts.length;
  }

  getNegativeCount(): number {
    return this.analysisResults.filter(a => a.sentiment === 'NEGATIVE').length;
  }

  getPositiveCount(): number {
    return this.analysisResults.filter(a => a.sentiment === 'POSITIVE').length;
  }

  getNeutralCount(): number {
    return this.analysisResults.filter(a => a.sentiment === 'NEUTRAL').length;
  }

  getHighAlertsCount(): number {
    return this.alerts.filter(a => a.severity === 'HIGH' || a.severity === 'CRITICAL').length;
  }

  getSourcesCount(): number {
    const set = new Set(this.newsItems.map(n => n.dataSource?.id).filter(x => x != null));

    return set.size;
  }

  getCompetitorsCount(): number {
    const set = new Set(this.newsItems.map(n => n.competitor?.id).filter(x => x != null));

    return set.size;
  }

  // =======================
  // Последние записи
  // =======================

  getLatestNews(): any[] {
    return [...this.newsItems].sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()).slice(0, 5);
  }

  getLatestAnalysis(): any[] {
    return [...this.analysisResults].reverse().slice(0, 5);
  }

  getLatestAlerts(): any[] {
    return [...this.alerts].reverse().slice(0, 5);
  }

  getLastRun(): any {
    if (!this.collectionRuns.length) {
      return null;
    }

    return [...this.collectionRuns].reverse()[0];
  }

  // =======================
  // Проценты для диаграммы
  // =======================

  getPositivePercent(): number {
    if (!this.analysisResults.length) return 0;

    return Math.round((this.getPositiveCount() / this.analysisResults.length) * 100);
  }

  getNeutralPercent(): number {
    if (!this.analysisResults.length) return 0;

    return Math.round((this.getNeutralCount() / this.analysisResults.length) * 100);
  }

  getNegativePercent(): number {
    if (!this.analysisResults.length) return 0;

    return Math.round((this.getNegativeCount() / this.analysisResults.length) * 100);
  }

  // =======================
  // Цвет тональности
  // =======================

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

  // =======================
  // Цвет Alert
  // =======================

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
