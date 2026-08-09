import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { ICollectionRun } from 'app/entities/collection-run/collection-run.model';
import { CollectionRunService } from 'app/entities/collection-run/service/collection-run.service';
import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { IDataSource } from 'app/entities/data-source/data-source.model';
import { DataSourceService } from 'app/entities/data-source/service/data-source.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { INewsItem } from '../news-item.model';
import { NewsItemService } from '../service/news-item.service';

import { NewsItemFormGroup, NewsItemFormService } from './news-item-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-news-item-update',
  templateUrl: './news-item-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class NewsItemUpdate implements OnInit {
  readonly isSaving = signal(false);
  newsItem: INewsItem | null = null;

  dataSourcesSharedCollection = signal<IDataSource[]>([]);
  competitorsSharedCollection = signal<ICompetitor[]>([]);
  collectionRunsSharedCollection = signal<ICollectionRun[]>([]);

  protected newsItemService = inject(NewsItemService);
  protected newsItemFormService = inject(NewsItemFormService);
  protected dataSourceService = inject(DataSourceService);
  protected competitorService = inject(CompetitorService);
  protected collectionRunService = inject(CollectionRunService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: NewsItemFormGroup = this.newsItemFormService.createNewsItemFormGroup();

  compareDataSource = (o1: IDataSource | null, o2: IDataSource | null): boolean => this.dataSourceService.compareDataSource(o1, o2);

  compareCompetitor = (o1: ICompetitor | null, o2: ICompetitor | null): boolean => this.competitorService.compareCompetitor(o1, o2);

  compareCollectionRun = (o1: ICollectionRun | null, o2: ICollectionRun | null): boolean =>
    this.collectionRunService.compareCollectionRun(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ newsItem }) => {
      this.newsItem = newsItem;
      if (newsItem) {
        this.updateForm(newsItem);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const newsItem = this.newsItemFormService.getNewsItem(this.editForm);
    if (newsItem.id === null) {
      this.subscribeToSaveResponse(this.newsItemService.create(newsItem));
    } else {
      this.subscribeToSaveResponse(this.newsItemService.update(newsItem));
    }
  }

  protected subscribeToSaveResponse(result: Observable<INewsItem | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(newsItem: INewsItem): void {
    this.newsItem = newsItem;
    this.newsItemFormService.resetForm(this.editForm, newsItem);

    this.dataSourcesSharedCollection.update(dataSources =>
      this.dataSourceService.addDataSourceToCollectionIfMissing<IDataSource>(dataSources, newsItem.dataSource),
    );
    this.competitorsSharedCollection.update(competitors =>
      this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(competitors, newsItem.competitor),
    );
    this.collectionRunsSharedCollection.update(collectionRuns =>
      this.collectionRunService.addCollectionRunToCollectionIfMissing<ICollectionRun>(collectionRuns, newsItem.collectionRun),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.dataSourceService
      .query({
        size: 1000,
      })
      .pipe(map((res: HttpResponse<IDataSource[]>) => res.body ?? []))
      .pipe(
        map((dataSources: IDataSource[]) =>
          this.dataSourceService.addDataSourceToCollectionIfMissing<IDataSource>(dataSources, this.newsItem?.dataSource),
        ),
      )
      .subscribe((dataSources: IDataSource[]) => this.dataSourcesSharedCollection.set(dataSources));

    this.competitorService
      .query({
        size: 1000,
      })
      .pipe(map((res: HttpResponse<ICompetitor[]>) => res.body ?? []))
      .pipe(
        map((competitors: ICompetitor[]) =>
          this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(competitors, this.newsItem?.competitor),
        ),
      )
      .subscribe((competitors: ICompetitor[]) => this.competitorsSharedCollection.set(competitors));

    this.collectionRunService
      .query({
        size: 1000,
      })
      .pipe(map((res: HttpResponse<ICollectionRun[]>) => res.body ?? []))
      .pipe(
        map((collectionRuns: ICollectionRun[]) =>
          this.collectionRunService.addCollectionRunToCollectionIfMissing<ICollectionRun>(collectionRuns, this.newsItem?.collectionRun),
        ),
      )
      .subscribe((collectionRuns: ICollectionRun[]) => this.collectionRunsSharedCollection.set(collectionRuns));
  }
}
