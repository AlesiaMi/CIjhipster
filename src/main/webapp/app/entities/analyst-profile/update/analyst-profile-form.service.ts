import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAnalystProfile, NewAnalystProfile } from '../analyst-profile.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAnalystProfile for edit and NewAnalystProfileFormGroupInput for create.
 */
type AnalystProfileFormGroupInput = IAnalystProfile | PartialWithRequiredKeyOf<NewAnalystProfile>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAnalystProfile | NewAnalystProfile> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type AnalystProfileFormRawValue = FormValueOf<IAnalystProfile>;

type NewAnalystProfileFormRawValue = FormValueOf<NewAnalystProfile>;

type AnalystProfileFormDefaults = Pick<NewAnalystProfile, 'id' | 'notificationEnabled' | 'createdAt' | 'competitorses'>;

type AnalystProfileFormGroupContent = {
  id: FormControl<AnalystProfileFormRawValue['id'] | NewAnalystProfile['id']>;
  displayName: FormControl<AnalystProfileFormRawValue['displayName']>;
  telegramChatId: FormControl<AnalystProfileFormRawValue['telegramChatId']>;
  notificationEnabled: FormControl<AnalystProfileFormRawValue['notificationEnabled']>;
  createdAt: FormControl<AnalystProfileFormRawValue['createdAt']>;
  user: FormControl<AnalystProfileFormRawValue['user']>;
  competitorses: FormControl<AnalystProfileFormRawValue['competitorses']>;
};

export type AnalystProfileFormGroup = FormGroup<AnalystProfileFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AnalystProfileFormService {
  createAnalystProfileFormGroup(analystProfile?: AnalystProfileFormGroupInput): AnalystProfileFormGroup {
    const analystProfileRawValue = this.convertAnalystProfileToAnalystProfileRawValue({
      ...this.getFormDefaults(),
      ...(analystProfile ?? { id: null }),
    });
    return new FormGroup<AnalystProfileFormGroupContent>({
      id: new FormControl(
        { value: analystProfileRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      displayName: new FormControl(analystProfileRawValue.displayName, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      telegramChatId: new FormControl(analystProfileRawValue.telegramChatId, {
        validators: [Validators.maxLength(100)],
      }),
      notificationEnabled: new FormControl(analystProfileRawValue.notificationEnabled, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(analystProfileRawValue.createdAt, {
        validators: [Validators.required],
      }),
      user: new FormControl(analystProfileRawValue.user, {
        validators: [Validators.required],
      }),
      competitorses: new FormControl(analystProfileRawValue.competitorses ?? []),
    });
  }

  getAnalystProfile(form: AnalystProfileFormGroup): IAnalystProfile | NewAnalystProfile {
    return this.convertAnalystProfileRawValueToAnalystProfile(form.getRawValue());
  }

  resetForm(form: AnalystProfileFormGroup, analystProfile: AnalystProfileFormGroupInput): void {
    const analystProfileRawValue = this.convertAnalystProfileToAnalystProfileRawValue({ ...this.getFormDefaults(), ...analystProfile });
    form.reset({
      ...analystProfileRawValue,
      id: { value: analystProfileRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): AnalystProfileFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      notificationEnabled: false,
      createdAt: currentTime,
      competitorses: [],
    };
  }

  private convertAnalystProfileRawValueToAnalystProfile(
    rawAnalystProfile: AnalystProfileFormRawValue | NewAnalystProfileFormRawValue,
  ): IAnalystProfile | NewAnalystProfile {
    return {
      ...rawAnalystProfile,
      createdAt: dayjs(rawAnalystProfile.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertAnalystProfileToAnalystProfileRawValue(
    analystProfile: IAnalystProfile | (Partial<NewAnalystProfile> & AnalystProfileFormDefaults),
  ): AnalystProfileFormRawValue | PartialWithRequiredKeyOf<NewAnalystProfileFormRawValue> {
    return {
      ...analystProfile,
      createdAt: analystProfile.createdAt ? analystProfile.createdAt.format(DATE_TIME_FORMAT) : undefined,
      competitorses: analystProfile.competitorses ?? [],
    };
  }
}
