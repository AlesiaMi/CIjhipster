import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ICiAlert } from '../ci-alert.model';

@Component({
  selector: 'jhi-ci-alert-detail',
  templateUrl: './ci-alert-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class CiAlertDetail {
  readonly ciAlert = input<ICiAlert | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
