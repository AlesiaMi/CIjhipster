import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { INewsItem, NewNewsItem } from '../news-item.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts INewsItem for edit and NewNewsItemFormGroupInput for create.
 */
type NewsItemFormGroupInput = INewsItem | PartialWithRequiredKeyOf<NewNewsItem>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends INewsItem | NewNewsItem> = Omit<T, 'publishedAt' | 'collectedAt'> & {
  publishedAt?: string | null;
  collectedAt?: string | null;
};

type NewsItemFormRawValue = FormValueOf<INewsItem>;

type NewNewsItemFormRawValue = FormValueOf<NewNewsItem>;

type NewsItemFormDefaults = Pick<NewNewsItem, 'id' | 'publishedAt' | 'collectedAt' | 'isDuplicate'>;

type NewsItemFormGroupContent = {
  id: FormControl<NewsItemFormRawValue['id'] | NewNewsItem['id']>;
  externalId: FormControl<NewsItemFormRawValue['externalId']>;
  title: FormControl<NewsItemFormRawValue['title']>;
  url: FormControl<NewsItemFormRawValue['url']>;
  originalText: FormControl<NewsItemFormRawValue['originalText']>;
  publishedAt: FormControl<NewsItemFormRawValue['publishedAt']>;
  collectedAt: FormControl<NewsItemFormRawValue['collectedAt']>;
  isDuplicate: FormControl<NewsItemFormRawValue['isDuplicate']>;
  dataSource: FormControl<NewsItemFormRawValue['dataSource']>;
  competitor: FormControl<NewsItemFormRawValue['competitor']>;
  collectionRun: FormControl<NewsItemFormRawValue['collectionRun']>;
};

export type NewsItemFormGroup = FormGroup<NewsItemFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class NewsItemFormService {
  createNewsItemFormGroup(newsItem?: NewsItemFormGroupInput): NewsItemFormGroup {
    const newsItemRawValue = this.convertNewsItemToNewsItemRawValue({
      ...this.getFormDefaults(),
      ...(newsItem ?? { id: null }),
    });
    return new FormGroup<NewsItemFormGroupContent>({
      id: new FormControl(
        { value: newsItemRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      externalId: new FormControl(newsItemRawValue.externalId, {
        validators: [Validators.maxLength(255)],
      }),
      title: new FormControl(newsItemRawValue.title, {
        validators: [Validators.required, Validators.maxLength(500)],
      }),
      url: new FormControl(newsItemRawValue.url, {
        validators: [Validators.required, Validators.maxLength(1000)],
      }),
      originalText: new FormControl(newsItemRawValue.originalText, {
        validators: [Validators.maxLength(10000)],
      }),
      publishedAt: new FormControl(newsItemRawValue.publishedAt),
      collectedAt: new FormControl(newsItemRawValue.collectedAt, {
        validators: [Validators.required],
      }),
      isDuplicate: new FormControl(newsItemRawValue.isDuplicate, {
        validators: [Validators.required],
      }),
      dataSource: new FormControl(newsItemRawValue.dataSource, {
        validators: [Validators.required],
      }),
      competitor: new FormControl(newsItemRawValue.competitor, {
        validators: [Validators.required],
      }),
      collectionRun: new FormControl(newsItemRawValue.collectionRun),
    });
  }

  getNewsItem(form: NewsItemFormGroup): INewsItem | NewNewsItem {
    return this.convertNewsItemRawValueToNewsItem(form.getRawValue());
  }

  resetForm(form: NewsItemFormGroup, newsItem: NewsItemFormGroupInput): void {
    const newsItemRawValue = this.convertNewsItemToNewsItemRawValue({ ...this.getFormDefaults(), ...newsItem });
    form.reset({
      ...newsItemRawValue,
      id: { value: newsItemRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): NewsItemFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      publishedAt: currentTime,
      collectedAt: currentTime,
      isDuplicate: false,
    };
  }

  private convertNewsItemRawValueToNewsItem(rawNewsItem: NewsItemFormRawValue | NewNewsItemFormRawValue): INewsItem | NewNewsItem {
    return {
      ...rawNewsItem,
      publishedAt: dayjs(rawNewsItem.publishedAt, DATE_TIME_FORMAT),
      collectedAt: dayjs(rawNewsItem.collectedAt, DATE_TIME_FORMAT),
    };
  }

  private convertNewsItemToNewsItemRawValue(
    newsItem: INewsItem | (Partial<NewNewsItem> & NewsItemFormDefaults),
  ): NewsItemFormRawValue | PartialWithRequiredKeyOf<NewNewsItemFormRawValue> {
    return {
      ...newsItem,
      publishedAt: newsItem.publishedAt ? newsItem.publishedAt.format(DATE_TIME_FORMAT) : undefined,
      collectedAt: newsItem.collectedAt ? newsItem.collectedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
