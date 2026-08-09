import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IAnalystProfile } from '../analyst-profile.model';
import { AnalystProfileService } from '../service/analyst-profile.service';

import { AnalystProfileFormGroup, AnalystProfileFormService } from './analyst-profile-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-analyst-profile-update',
  templateUrl: './analyst-profile-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class AnalystProfileUpdate implements OnInit {
  readonly isSaving = signal(false);
  analystProfile: IAnalystProfile | null = null;

  usersSharedCollection = signal<IUser[]>([]);
  competitorsSharedCollection = signal<ICompetitor[]>([]);

  protected analystProfileService = inject(AnalystProfileService);
  protected analystProfileFormService = inject(AnalystProfileFormService);
  protected userService = inject(UserService);
  protected competitorService = inject(CompetitorService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AnalystProfileFormGroup = this.analystProfileFormService.createAnalystProfileFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  compareCompetitor = (o1: ICompetitor | null, o2: ICompetitor | null): boolean => this.competitorService.compareCompetitor(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ analystProfile }) => {
      this.analystProfile = analystProfile;
      if (analystProfile) {
        this.updateForm(analystProfile);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const analystProfile = this.analystProfileFormService.getAnalystProfile(this.editForm);
    if (analystProfile.id === null) {
      this.subscribeToSaveResponse(this.analystProfileService.create(analystProfile));
    } else {
      this.subscribeToSaveResponse(this.analystProfileService.update(analystProfile));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IAnalystProfile | null>): void {
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

  protected updateForm(analystProfile: IAnalystProfile): void {
    this.analystProfile = analystProfile;
    this.analystProfileFormService.resetForm(this.editForm, analystProfile);

    this.usersSharedCollection.update(users => this.userService.addUserToCollectionIfMissing<IUser>(users, analystProfile.user));
    this.competitorsSharedCollection.update(competitors =>
      this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(competitors, ...(analystProfile.competitorses ?? [])),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query({ size: 1000 })
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.analystProfile?.user)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));

    this.competitorService
      .query({ size: 1000 })
      .pipe(map((res: HttpResponse<ICompetitor[]>) => res.body ?? []))
      .pipe(
        map((competitors: ICompetitor[]) =>
          this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(
            competitors,
            ...(this.analystProfile?.competitorses ?? []),
          ),
        ),
      )
      .subscribe((competitors: ICompetitor[]) => this.competitorsSharedCollection.set(competitors));
  }
}
