import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IAnalysisResult } from 'app/entities/analysis-result/analysis-result.model';
import { AnalysisResultService } from 'app/entities/analysis-result/service/analysis-result.service';
import { IAnalystProfile } from 'app/entities/analyst-profile/analyst-profile.model';
import { AnalystProfileService } from 'app/entities/analyst-profile/service/analyst-profile.service';
import { AlertStatus } from 'app/entities/enumerations/alert-status.model';
import { Severity } from 'app/entities/enumerations/severity.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ICiAlert } from '../ci-alert.model';
import { CiAlertService } from '../service/ci-alert.service';

import { CiAlertFormGroup, CiAlertFormService } from './ci-alert-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-ci-alert-update',
  templateUrl: './ci-alert-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class CiAlertUpdate implements OnInit {
  readonly isSaving = signal(false);
  ciAlert: ICiAlert | null = null;
  severityValues = Object.keys(Severity);
  alertStatusValues = Object.keys(AlertStatus);

  analysisResultsSharedCollection = signal<IAnalysisResult[]>([]);
  analystProfilesSharedCollection = signal<IAnalystProfile[]>([]);

  protected ciAlertService = inject(CiAlertService);
  protected ciAlertFormService = inject(CiAlertFormService);
  protected analysisResultService = inject(AnalysisResultService);
  protected analystProfileService = inject(AnalystProfileService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CiAlertFormGroup = this.ciAlertFormService.createCiAlertFormGroup();

  compareAnalysisResult = (o1: IAnalysisResult | null, o2: IAnalysisResult | null): boolean =>
    this.analysisResultService.compareAnalysisResult(o1, o2);

  compareAnalystProfile = (o1: IAnalystProfile | null, o2: IAnalystProfile | null): boolean =>
    this.analystProfileService.compareAnalystProfile(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ ciAlert }) => {
      this.ciAlert = ciAlert;
      if (ciAlert) {
        this.updateForm(ciAlert);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const ciAlert = this.ciAlertFormService.getCiAlert(this.editForm);
    if (ciAlert.id === null) {
      this.subscribeToSaveResponse(this.ciAlertService.create(ciAlert));
    } else {
      this.subscribeToSaveResponse(this.ciAlertService.update(ciAlert));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ICiAlert | null>): void {
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

  protected updateForm(ciAlert: ICiAlert): void {
    this.ciAlert = ciAlert;
    this.ciAlertFormService.resetForm(this.editForm, ciAlert);

    this.analysisResultsSharedCollection.update(analysisResults =>
      this.analysisResultService.addAnalysisResultToCollectionIfMissing<IAnalysisResult>(analysisResults, ciAlert.analysisResult),
    );
    this.analystProfilesSharedCollection.update(analystProfiles =>
      this.analystProfileService.addAnalystProfileToCollectionIfMissing<IAnalystProfile>(analystProfiles, ciAlert.analystProfile),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.analysisResultService
      .query({
        size: 1000,
      })
      .pipe(map((res: HttpResponse<IAnalysisResult[]>) => res.body ?? []))
      .pipe(
        map((analysisResults: IAnalysisResult[]) =>
          this.analysisResultService.addAnalysisResultToCollectionIfMissing<IAnalysisResult>(analysisResults, this.ciAlert?.analysisResult),
        ),
      )
      .subscribe((analysisResults: IAnalysisResult[]) => this.analysisResultsSharedCollection.set(analysisResults));

    this.analystProfileService
      .query({
        size: 1000,
      })
      .pipe(map((res: HttpResponse<IAnalystProfile[]>) => res.body ?? []))
      .pipe(
        map((analystProfiles: IAnalystProfile[]) =>
          this.analystProfileService.addAnalystProfileToCollectionIfMissing<IAnalystProfile>(analystProfiles, this.ciAlert?.analystProfile),
        ),
      )
      .subscribe((analystProfiles: IAnalystProfile[]) => this.analystProfilesSharedCollection.set(analystProfiles));
  }
}
