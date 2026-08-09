import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize } from 'rxjs';

import { RunStatus } from 'app/entities/enumerations/run-status.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ICollectionRun } from '../collection-run.model';
import { CollectionRunService } from '../service/collection-run.service';

import { CollectionRunFormGroup, CollectionRunFormService } from './collection-run-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-collection-run-update',
  templateUrl: './collection-run-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class CollectionRunUpdate implements OnInit {
  readonly isSaving = signal(false);
  collectionRun: ICollectionRun | null = null;
  runStatusValues = Object.keys(RunStatus);

  protected collectionRunService = inject(CollectionRunService);
  protected collectionRunFormService = inject(CollectionRunFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CollectionRunFormGroup = this.collectionRunFormService.createCollectionRunFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ collectionRun }) => {
      this.collectionRun = collectionRun;
      if (collectionRun) {
        this.updateForm(collectionRun);
      }
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const collectionRun = this.collectionRunFormService.getCollectionRun(this.editForm);
    if (collectionRun.id === null) {
      this.subscribeToSaveResponse(this.collectionRunService.create(collectionRun));
    } else {
      this.subscribeToSaveResponse(this.collectionRunService.update(collectionRun));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ICollectionRun | null>): void {
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

  protected updateForm(collectionRun: ICollectionRun): void {
    this.collectionRun = collectionRun;
    this.collectionRunFormService.resetForm(this.editForm, collectionRun);
  }
}
