import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IAnalystProfile } from '../analyst-profile.model';

@Component({
  selector: 'jhi-analyst-profile-detail',
  templateUrl: './analyst-profile-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class AnalystProfileDetail {
  readonly analystProfile = input<IAnalystProfile | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
