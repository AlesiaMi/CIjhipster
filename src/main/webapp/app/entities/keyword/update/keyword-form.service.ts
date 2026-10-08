import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IKeyword, NewKeyword } from '../keyword.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IKeyword for edit and NewKeywordFormGroupInput for create.
 */
type KeywordFormGroupInput = IKeyword | PartialWithRequiredKeyOf<NewKeyword>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IKeyword | NewKeyword> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type KeywordFormRawValue = FormValueOf<IKeyword>;

type NewKeywordFormRawValue = FormValueOf<NewKeyword>;

type KeywordFormDefaults = Pick<NewKeyword, 'id' | 'isActive' | 'createdAt'>;

type KeywordFormGroupContent = {
  id: FormControl<KeywordFormRawValue['id'] | NewKeyword['id']>;
  value: FormControl<KeywordFormRawValue['value']>;
  isActive: FormControl<KeywordFormRawValue['isActive']>;
  createdAt: FormControl<KeywordFormRawValue['createdAt']>;
  competitor: FormControl<KeywordFormRawValue['competitor']>;
};

export type KeywordFormGroup = FormGroup<KeywordFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class KeywordFormService {
  createKeywordFormGroup(keyword?: KeywordFormGroupInput): KeywordFormGroup {
    const keywordRawValue = this.convertKeywordToKeywordRawValue({
      ...this.getFormDefaults(),
      ...(keyword ?? { id: null }),
    });
    return new FormGroup<KeywordFormGroupContent>({
      id: new FormControl(
        { value: keywordRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      value: new FormControl(keywordRawValue.value, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      isActive: new FormControl(keywordRawValue.isActive, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(keywordRawValue.createdAt, {
        validators: [Validators.required],
      }),
      competitor: new FormControl(keywordRawValue.competitor, {
        validators: [Validators.required],
      }),
    });
  }

  getKeyword(form: KeywordFormGroup): IKeyword | NewKeyword {
    return this.convertKeywordRawValueToKeyword(form.getRawValue());
  }

  resetForm(form: KeywordFormGroup, keyword: KeywordFormGroupInput): void {
    const keywordRawValue = this.convertKeywordToKeywordRawValue({ ...this.getFormDefaults(), ...keyword });
    form.reset({
      ...keywordRawValue,
      id: { value: keywordRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): KeywordFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      isActive: false,
      createdAt: currentTime,
    };
  }

  private convertKeywordRawValueToKeyword(rawKeyword: KeywordFormRawValue | NewKeywordFormRawValue): IKeyword | NewKeyword {
    return {
      ...rawKeyword,
      createdAt: dayjs(rawKeyword.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertKeywordToKeywordRawValue(
    keyword: IKeyword | (Partial<NewKeyword> & KeywordFormDefaults),
  ): KeywordFormRawValue | PartialWithRequiredKeyOf<NewKeywordFormRawValue> {
    return {
      ...keyword,
      createdAt: keyword.createdAt ? keyword.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
