import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';
import { combineLatest, filter, finalize, map, tap } from 'rxjs';

import { EntityImportService, ImportError } from 'app/shared/import/import.service';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { Filter, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';

import { IDataSource } from '../data-source.model';
import { DataSourceDeleteDialog } from '../delete/data-source-delete-dialog';
import { DataSourceService } from '../service/data-source.service';

@Component({
  selector: 'jhi-data-source',
  templateUrl: './data-source.html',
  imports: [
    RouterLink,
    FontAwesomeModule,
    AlertError,
    Alert,
    SortDirective,
    SortByDirective,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatetimePipe,
    Filter,
    NgbPagination,
    ItemCount,
  ],
})
export class DataSource {
  readonly dataSources = signal<IDataSource[]>([]);

  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();

  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);

  readonly router = inject(Router);

  protected readonly dataSourceService = inject(DataSourceService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  readonly isLoading = this.dataSourceService.dataSourcesResource.isLoading;

  protected readonly activatedRoute = inject(ActivatedRoute);

  protected readonly activatedRouteState = toSignal(
    combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data]).pipe(
      map(([queryParamMap, data]) => ({ queryParamMap, data })),
    ),
    {
      initialValue: {
        queryParamMap: this.activatedRoute.snapshot.queryParamMap,
        data: this.activatedRoute.snapshot.data,
      },
    },
  );

  protected readonly sortService = inject(SortService);
  protected readonly filterOptions = toSignal(this.filters.filterChanges);
  protected modalService = inject(NgbModal);

  protected readonly managerClientContext = inject(ManagerClientContextService);
  protected readonly entityImportService = inject(EntityImportService);

  readonly canEdit = computed(() => this.managerClientContext.hasPermission('SOURCES_EDIT'));

  readonly isImporting = signal(false);
  readonly bulkImport = signal(false);
  readonly importErrors = signal<ImportError[]>([]);
  readonly importSuccess = signal<string | null>(null);

  constructor() {
    // Инициализация контекста менеджера
    this.managerClientContext.initialize().subscribe();

    // Обновление количества элементов из заголовков ответа
    effect(() => {
      const headers = this.dataSourceService.dataSourcesResource.headers();

      if (headers) {
        this.fillComponentAttributesFromResponseHeader(headers);
      }
    });

    // Обновление списка источников
    effect(() => {
      this.dataSources.set(this.fillComponentAttributesFromResponseBody([...this.dataSourceService.dataSources()]));
    });

    // Обработка изменений маршрута после инициализации менеджера
    effect(() => {
      const activatedRouteState = this.activatedRouteState();

      if (!this.managerClientContext.initialized()) {
        return;
      }

      untracked(() => {
        this.fillComponentAttributeFromRoute(activatedRouteState.queryParamMap, activatedRouteState.data);

        this.load();
      });
    });

    // Обработка изменений фильтров
    effect(() => {
      const filterOptions = this.filterOptions();

      if (filterOptions) {
        untracked(() => {
          this.handleNavigation(1, this.sortState(), filterOptions);
        });
      }
    });
  }

  trackId = (item: IDataSource): number => this.dataSourceService.getDataSourceIdentifier(item);

  delete(dataSource: IDataSource): void {
    const modalRef = this.modalService.open(DataSourceDeleteDialog, {
      size: 'lg',
      backdrop: 'static',
    });

    modalRef.componentInstance.dataSource = dataSource;

    // unsubscribe не нужен: closed завершается после закрытия окна
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
      .importFile('data-source', file, this.bulkImport())
      .pipe(finalize(() => this.isImporting.set(false)))
      .subscribe({
        next: result => {
          this.importSuccess.set(`Импортировано источников: ${result.imported}`);
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

  protected fillComponentAttributesFromResponseBody(data: IDataSource[]): IDataSource[] {
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
      eagerload: true,
      sort: this.sortService.buildSortParam(this.sortState()),
    };

    for (const filterOption of this.filters.filterOptions) {
      queryObject[filterOption.name] = filterOption.values;
    }

    this.dataSourceService.dataSourcesParams.set(queryObject);
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
