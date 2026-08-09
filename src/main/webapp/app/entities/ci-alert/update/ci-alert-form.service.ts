import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ICiAlert, NewCiAlert } from '../ci-alert.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICiAlert for edit and NewCiAlertFormGroupInput for create.
 */
type CiAlertFormGroupInput = ICiAlert | PartialWithRequiredKeyOf<NewCiAlert>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ICiAlert | NewCiAlert> = Omit<T, 'createdAt' | 'readAt'> & {
  createdAt?: string | null;
  readAt?: string | null;
};

type CiAlertFormRawValue = FormValueOf<ICiAlert>;

type NewCiAlertFormRawValue = FormValueOf<NewCiAlert>;

type CiAlertFormDefaults = Pick<NewCiAlert, 'id' | 'createdAt' | 'readAt'>;

type CiAlertFormGroupContent = {
  id: FormControl<CiAlertFormRawValue['id'] | NewCiAlert['id']>;
  title: FormControl<CiAlertFormRawValue['title']>;
  message: FormControl<CiAlertFormRawValue['message']>;
  severity: FormControl<CiAlertFormRawValue['severity']>;
  status: FormControl<CiAlertFormRawValue['status']>;
  createdAt: FormControl<CiAlertFormRawValue['createdAt']>;
  readAt: FormControl<CiAlertFormRawValue['readAt']>;
  analysisResult: FormControl<CiAlertFormRawValue['analysisResult']>;
  analystProfile: FormControl<CiAlertFormRawValue['analystProfile']>;
};

export type CiAlertFormGroup = FormGroup<CiAlertFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CiAlertFormService {
  createCiAlertFormGroup(ciAlert?: CiAlertFormGroupInput): CiAlertFormGroup {
    const ciAlertRawValue = this.convertCiAlertToCiAlertRawValue({
      ...this.getFormDefaults(),
      ...(ciAlert ?? { id: null }),
    });
    return new FormGroup<CiAlertFormGroupContent>({
      id: new FormControl(
        { value: ciAlertRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(ciAlertRawValue.title, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      message: new FormControl(ciAlertRawValue.message, {
        validators: [Validators.required, Validators.maxLength(4000)],
      }),
      severity: new FormControl(ciAlertRawValue.severity, {
        validators: [Validators.required],
      }),
      status: new FormControl(ciAlertRawValue.status, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(ciAlertRawValue.createdAt, {
        validators: [Validators.required],
      }),
      readAt: new FormControl(ciAlertRawValue.readAt),
      analysisResult: new FormControl(ciAlertRawValue.analysisResult, {
        validators: [Validators.required],
      }),
      analystProfile: new FormControl(ciAlertRawValue.analystProfile, {
        validators: [Validators.required],
      }),
    });
  }

  getCiAlert(form: CiAlertFormGroup): ICiAlert | NewCiAlert {
    return this.convertCiAlertRawValueToCiAlert(form.getRawValue());
  }

  resetForm(form: CiAlertFormGroup, ciAlert: CiAlertFormGroupInput): void {
    const ciAlertRawValue = this.convertCiAlertToCiAlertRawValue({ ...this.getFormDefaults(), ...ciAlert });
    form.reset({
      ...ciAlertRawValue,
      id: { value: ciAlertRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): CiAlertFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      readAt: currentTime,
    };
  }

  private convertCiAlertRawValueToCiAlert(rawCiAlert: CiAlertFormRawValue | NewCiAlertFormRawValue): ICiAlert | NewCiAlert {
    return {
      ...rawCiAlert,
      createdAt: dayjs(rawCiAlert.createdAt, DATE_TIME_FORMAT),
      readAt: dayjs(rawCiAlert.readAt, DATE_TIME_FORMAT),
    };
  }

  private convertCiAlertToCiAlertRawValue(
    ciAlert: ICiAlert | (Partial<NewCiAlert> & CiAlertFormDefaults),
  ): CiAlertFormRawValue | PartialWithRequiredKeyOf<NewCiAlertFormRawValue> {
    return {
      ...ciAlert,
      createdAt: ciAlert.createdAt ? ciAlert.createdAt.format(DATE_TIME_FORMAT) : undefined,
      readAt: ciAlert.readAt ? ciAlert.readAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
