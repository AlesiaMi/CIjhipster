import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { INewsItem } from '../news-item.model';
import { AccountService } from 'app/core/auth/account.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-news-item-detail',
  templateUrl: './news-item-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, TranslateModule, RouterLink, FormatMediumDatetimePipe],
})
export class NewsItemDetail {
  readonly newsItem = input<INewsItem | null>(null);
  protected readonly accountService = inject(AccountService);
  readonly canEdit = computed(() => this.accountService.hasAnyAuthority('ROLE_ADMIN'));

  previousState(): void {
    globalThis.history.back();
  }
}
