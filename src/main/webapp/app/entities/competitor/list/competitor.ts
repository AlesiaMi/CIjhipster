import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, combineLatest, filter, finalize, tap } from 'rxjs';
import { EntityImportService, ImportError } from 'app/shared/import/import.service';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { Filter, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { ICompetitor } from '../competitor.model';
import { CompetitorDeleteDialog } from '../delete/competitor-delete-dialog';
import { CompetitorService } from '../service/competitor.service';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-competitor',
  templateUrl: './competitor.html',
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    SortDirective,
    SortByDirective,
    TranslateDirective,
    TranslateModule,
    Filter,
    NgbPagination,
    ItemCount,
  ],
})
export class Competitor implements OnInit {
  subscription: Subscription | null = null;
  readonly competitors = signal<ICompetitor[]>([]);

  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();

  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);

  readonly router = inject(Router);
  protected readonly competitorService = inject(CompetitorService);
  // eslint-disable-next-line @typescript-eslint/member-ordering
  readonly isLoading = this.competitorService.competitorsResource.isLoading;
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected readonly filterOptions = toSignal(this.filters.filterChanges);
  protected modalService = inject(NgbModal);
  protected readonly managerClientContext = inject(ManagerClientContextService);
  protected readonly entityImportService = inject(EntityImportService);
  readonly canEdit = computed(() => this.managerClientContext.hasPermission('COMPETITORS_EDIT'));
  readonly isImporting = signal(false);
  readonly importErrors = signal<ImportError[]>([]);
  readonly importSuccess = signal<string | null>(null);
  constructor() {
    effect(() => {
      const headers = this.competitorService.competitorsResource.headers();
      if (headers) {
        this.fillComponentAttributesFromResponseHeader(headers);
      }
    });
    effect(() => {
      this.competitors.set(this.fillComponentAttributesFromResponseBody([...this.competitorService.competitors()]));
    });

    effect(() => {
      const filterOptions = this.filterOptions();
      if (filterOptions) {
        untracked(() => {
          // Only watch for filter changes. Other signals should be ignored.
          this.handleNavigation(1, this.sortState(), filterOptions);
        });
      }
    });
  }

  trackId = (item: ICompetitor): number => this.competitorService.getCompetitorIdentifier(item);

  ngOnInit(): void {
    this.managerClientContext.initialize().subscribe(() => {
      this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
        .pipe(
          tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
          tap(() => this.load()),
        )
        .subscribe();
    });
  }

  delete(competitor: ICompetitor): void {
    const modalRef = this.modalService.open(CompetitorDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.competitor = competitor;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend();
  }

  importFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    input.value = '';
    if (!file) {
      return;
    }
    this.importErrors.set([]);
    this.importSuccess.set(null);
    this.isImporting.set(true);
    this.entityImportService
      .importFile('competitor', file)
      .pipe(finalize(() => this.isImporting.set(false)))
      .subscribe({
        next: result => {
          this.importSuccess.set(`Импортировано конкурентов: ${result.imported}`);
          this.load();
        },

        error: (error: HttpErrorResponse) => {
          const importErrors = error.error?.errors;
          if (Array.isArray(importErrors)) {
            this.importErrors.set(importErrors as ImportError[]);
            return;
          }
          this.importErrors.set([
            {
              message: 'Не удалось импортировать файл.',
            },
          ]);
        },
      });
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page(), event, this.filters.filterOptions);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState(), this.filters.filterOptions);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page.set(+(page ?? 1));
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.filters.initializeFromParams(params);
  }

  protected fillComponentAttributesFromResponseBody(data: ICompetitor[]): ICompetitor[] {
    return data;
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems.set(Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
  }

  protected queryBackend(): void {
    const pageToLoad: number = this.page();
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage(),
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    for (const filterOption of this.filters.filterOptions) {
      queryObject[filterOption.name] = filterOption.values;
    }
    this.competitorService.competitorsParams.set(queryObject);
  }

  protected handleNavigation(page: number, sortState: SortState, filterOptions?: IFilterOption[]): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage(),
      sort: this.sortService.buildSortParam(sortState),
    };

    if (filterOptions) {
      for (const filterOption of filterOptions) {
        queryParamsObj[filterOption.nameAsQueryParam()] = filterOption.values;
      }
    }

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }
}
