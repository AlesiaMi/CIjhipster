import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { TranslateDirective } from 'app/shared/language';
import { ICompetitor } from '../competitor.model';

@Component({
  selector: 'jhi-competitor-detail',
  templateUrl: './competitor-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink],
})
export class CompetitorDetail {
  readonly competitor = input<ICompetitor | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
