import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IKeyword } from '../keyword.model';

@Component({
  selector: 'jhi-keyword-detail',
  templateUrl: './keyword-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class KeywordDetail {
  readonly keyword = input<IKeyword | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
