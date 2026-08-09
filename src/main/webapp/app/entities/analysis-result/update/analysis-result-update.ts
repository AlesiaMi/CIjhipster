import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { AnalysisStatus } from 'app/entities/enumerations/analysis-status.model';
import { Sentiment } from 'app/entities/enumerations/sentiment.model';
import { INewsItem } from 'app/entities/news-item/news-item.model';
import { NewsItemService } from 'app/entities/news-item/service/news-item.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IAnalysisResult } from '../analysis-result.model';
import { AnalysisResultService } from '../service/analysis-result.service';

import { AnalysisResultFormGroup, AnalysisResultFormService } from './analysis-result-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-analysis-result-update',
  templateUrl: './analysis-result-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class AnalysisResultUpdate implements OnInit {
  readonly isSaving = signal(false);
  analysisResult: IAnalysisResult | null = null;
  sentimentValues = Object.keys(Sentiment);
  analysisStatusValues = Object.keys(AnalysisStatus);

  newsItemsCollection = signal<INewsItem[]>([]);

  protected analysisResultService = inject(AnalysisResultService);
  protected analysisResultFormService = inject(AnalysisResultFormService);
  protected newsItemService = inject(NewsItemService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AnalysisResultFormGroup = this.analysisResultFormService.createAnalysisResultFormGroup();

  compareNewsItem = (o1: INewsItem | null, o2: INewsItem | null): boolean => this.newsItemService.compareNewsItem(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ analysisResult }) => {
      this.analysisResult = analysisResult;
      if (analysisResult) {
        this.updateForm(analysisResult);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const analysisResult = this.analysisResultFormService.getAnalysisResult(this.editForm);
    if (analysisResult.id === null) {
      this.subscribeToSaveResponse(this.analysisResultService.create(analysisResult));
    } else {
      this.subscribeToSaveResponse(this.analysisResultService.update(analysisResult));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IAnalysisResult | null>): void {
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

  protected updateForm(analysisResult: IAnalysisResult): void {
    this.analysisResult = analysisResult;
    this.analysisResultFormService.resetForm(this.editForm, analysisResult);

    this.newsItemsCollection.set(
      this.newsItemService.addNewsItemToCollectionIfMissing<INewsItem>(this.newsItemsCollection(), analysisResult.newsItem),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.newsItemService
      .query({ 'analysisResultId.specified': 'false' })
      .pipe(map((res: HttpResponse<INewsItem[]>) => res.body ?? []))
      .pipe(
        map((newsItems: INewsItem[]) =>
          this.newsItemService.addNewsItemToCollectionIfMissing<INewsItem>(newsItems, this.analysisResult?.newsItem),
        ),
      )
      .subscribe((newsItems: INewsItem[]) => this.newsItemsCollection.set(newsItems));
  }
}
