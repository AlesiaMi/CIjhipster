import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IAnalystProfile } from 'app/entities/analyst-profile/analyst-profile.model';
import { AnalystProfileService } from 'app/entities/analyst-profile/service/analyst-profile.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ICompetitor } from '../competitor.model';
import { CompetitorService } from '../service/competitor.service';

import { CompetitorFormGroup, CompetitorFormService } from './competitor-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-competitor-update',
  templateUrl: './competitor-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class CompetitorUpdate implements OnInit {
  readonly isSaving = signal(false);
  competitor: ICompetitor | null = null;

  analystProfilesSharedCollection = signal<IAnalystProfile[]>([]);

  protected competitorService = inject(CompetitorService);
  protected competitorFormService = inject(CompetitorFormService);
  protected analystProfileService = inject(AnalystProfileService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CompetitorFormGroup = this.competitorFormService.createCompetitorFormGroup();

  compareAnalystProfile = (o1: IAnalystProfile | null, o2: IAnalystProfile | null): boolean =>
    this.analystProfileService.compareAnalystProfile(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ competitor }) => {
      this.competitor = competitor;
      if (competitor) {
        this.updateForm(competitor);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const competitor = this.competitorFormService.getCompetitor(this.editForm);
    if (competitor.id === null) {
      this.subscribeToSaveResponse(this.competitorService.create(competitor));
    } else {
      this.subscribeToSaveResponse(this.competitorService.update(competitor));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ICompetitor | null>): void {
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

  protected updateForm(competitor: ICompetitor): void {
    this.competitor = competitor;
    this.competitorFormService.resetForm(this.editForm, competitor);

    this.analystProfilesSharedCollection.update(analystProfiles =>
      this.analystProfileService.addAnalystProfileToCollectionIfMissing<IAnalystProfile>(
        analystProfiles,
        ...(competitor.analystProfileses ?? []),
      ),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.analystProfileService
      .query({
        size: 1000,
      })
      .pipe(map((res: HttpResponse<IAnalystProfile[]>) => res.body ?? []))
      .pipe(
        map((analystProfiles: IAnalystProfile[]) =>
          this.analystProfileService.addAnalystProfileToCollectionIfMissing<IAnalystProfile>(
            analystProfiles,
            ...(this.competitor?.analystProfileses ?? []),
          ),
        ),
      )
      .subscribe((analystProfiles: IAnalystProfile[]) => this.analystProfilesSharedCollection.set(analystProfiles));
  }
}
