import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IDataSource } from '../data-source.model';

@Component({
  selector: 'jhi-data-source-detail',
  templateUrl: './data-source-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class DataSourceDetail {
  readonly dataSource = input<IDataSource | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
