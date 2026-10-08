import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { Alert, AlertError } from 'app/shared/alert';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { INewsItem } from '../news-item.model';

@Component({
  selector: 'jhi-news-item-detail',
  templateUrl: './news-item-detail.html',
  imports: [FontAwesomeModule, Alert, AlertError, TranslateDirective, RouterLink, FormatMediumDatetimePipe],
})
export class NewsItemDetail {
  readonly newsItem = input<INewsItem | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}
