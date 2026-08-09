import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IKeyword } from '../keyword.model';
import { KeywordService } from '../service/keyword.service';

import { KeywordFormGroup, KeywordFormService } from './keyword-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-keyword-update',
  templateUrl: './keyword-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class KeywordUpdate implements OnInit {
  readonly isSaving = signal(false);
  keyword: IKeyword | null = null;

  competitorsSharedCollection = signal<ICompetitor[]>([]);

  protected keywordService = inject(KeywordService);
  protected keywordFormService = inject(KeywordFormService);
  protected competitorService = inject(CompetitorService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: KeywordFormGroup = this.keywordFormService.createKeywordFormGroup();

  compareCompetitor = (o1: ICompetitor | null, o2: ICompetitor | null): boolean => this.competitorService.compareCompetitor(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ keyword }) => {
      this.keyword = keyword;
      if (keyword) {
        this.updateForm(keyword);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const keyword = this.keywordFormService.getKeyword(this.editForm);
    if (keyword.id === null) {
      this.subscribeToSaveResponse(this.keywordService.create(keyword));
    } else {
      this.subscribeToSaveResponse(this.keywordService.update(keyword));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IKeyword | null>): void {
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

  protected updateForm(keyword: IKeyword): void {
    this.keyword = keyword;
    this.keywordFormService.resetForm(this.editForm, keyword);

    this.competitorsSharedCollection.update(competitors =>
      this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(competitors, keyword.competitor),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.competitorService
      .query({
        size: 1000,
      })
      .pipe(map((res: HttpResponse<ICompetitor[]>) => res.body ?? []))
      .pipe(
        map((competitors: ICompetitor[]) =>
          this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(competitors, this.keyword?.competitor),
        ),
      )
      .subscribe((competitors: ICompetitor[]) => this.competitorsSharedCollection.set(competitors));
  }
}
