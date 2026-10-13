import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { AccountService } from 'app/core/auth/account.service';
import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IAnalysisResult } from '../analysis-result.model';

@Component({
  selector: 'jhi-analysis-result-detail',
  templateUrl: './analysis-result-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class AnalysisResultDetail {
  readonly analysisResult = input<IAnalysisResult | null>(null);

  readonly canEdit = computed(() => this.accountService.hasAnyAuthority('ROLE_ADMIN'));
  protected readonly accountService = inject(AccountService);

  previousState(): void {
    globalThis.history.back();
  }
}
