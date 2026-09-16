import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { SourceType } from 'app/entities/enumerations/source-type.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IDataSource } from '../data-source.model';
import { DataSourceService } from '../service/data-source.service';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';
import { DataSourceFormGroup, DataSourceFormService } from './data-source-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-data-source-update',
  templateUrl: './data-source-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class DataSourceUpdate implements OnInit {
  readonly isSaving = signal(false);
  dataSource: IDataSource | null = null;
  sourceTypeValues = Object.keys(SourceType);

  competitorsSharedCollection = signal<ICompetitor[]>([]);

  protected dataSourceService = inject(DataSourceService);
  protected dataSourceFormService = inject(DataSourceFormService);
  protected competitorService = inject(CompetitorService);
  protected activatedRoute = inject(ActivatedRoute);
  protected readonly managerClientContext = inject(ManagerClientContextService);
  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: DataSourceFormGroup = this.dataSourceFormService.createDataSourceFormGroup();

  compareCompetitor = (o1: ICompetitor | null, o2: ICompetitor | null): boolean => this.competitorService.compareCompetitor(o1, o2);

  ngOnInit(): void {
    this.managerClientContext.initialize().subscribe(() => {
      this.activatedRoute.data.subscribe(({ dataSource }) => {
        this.dataSource = dataSource;

        if (dataSource) {
          this.updateForm(dataSource);
        }

        this.loadRelationshipsOptions();
      });
    });
  }
  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const dataSource = this.dataSourceFormService.getDataSource(this.editForm);
    if (dataSource.id === null) {
      this.subscribeToSaveResponse(this.dataSourceService.create(dataSource));
    } else {
      this.subscribeToSaveResponse(this.dataSourceService.update(dataSource));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IDataSource | null>): void {
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

  protected updateForm(dataSource: IDataSource): void {
    this.dataSource = dataSource;
    this.dataSourceFormService.resetForm(this.editForm, dataSource);

    this.competitorsSharedCollection.update(competitors =>
      this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(competitors, dataSource.competitor),
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
          this.competitorService.addCompetitorToCollectionIfMissing<ICompetitor>(competitors, this.dataSource?.competitor),
        ),
      )
      .subscribe((competitors: ICompetitor[]) => this.competitorsSharedCollection.set(competitors));
  }
}
