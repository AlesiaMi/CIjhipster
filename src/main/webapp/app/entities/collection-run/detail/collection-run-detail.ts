import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ICollectionRun } from '../collection-run.model';

@Component({
  selector: 'jhi-collection-run-detail',
  templateUrl: './collection-run-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class CollectionRunDetail {
  readonly collectionRun = input<ICollectionRun | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
