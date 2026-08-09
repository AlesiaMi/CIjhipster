import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ICollectionRun, NewCollectionRun } from '../collection-run.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICollectionRun for edit and NewCollectionRunFormGroupInput for create.
 */
type CollectionRunFormGroupInput = ICollectionRun | PartialWithRequiredKeyOf<NewCollectionRun>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ICollectionRun | NewCollectionRun> = Omit<T, 'startedAt' | 'finishedAt'> & {
  startedAt?: string | null;
  finishedAt?: string | null;
};

type CollectionRunFormRawValue = FormValueOf<ICollectionRun>;

type NewCollectionRunFormRawValue = FormValueOf<NewCollectionRun>;

type CollectionRunFormDefaults = Pick<NewCollectionRun, 'id' | 'startedAt' | 'finishedAt'>;

type CollectionRunFormGroupContent = {
  id: FormControl<CollectionRunFormRawValue['id'] | NewCollectionRun['id']>;
  startedAt: FormControl<CollectionRunFormRawValue['startedAt']>;
  finishedAt: FormControl<CollectionRunFormRawValue['finishedAt']>;
  status: FormControl<CollectionRunFormRawValue['status']>;
  foundCount: FormControl<CollectionRunFormRawValue['foundCount']>;
  processedCount: FormControl<CollectionRunFormRawValue['processedCount']>;
  errorMessage: FormControl<CollectionRunFormRawValue['errorMessage']>;
};

export type CollectionRunFormGroup = FormGroup<CollectionRunFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CollectionRunFormService {
  createCollectionRunFormGroup(collectionRun?: CollectionRunFormGroupInput): CollectionRunFormGroup {
    const collectionRunRawValue = this.convertCollectionRunToCollectionRunRawValue({
      ...this.getFormDefaults(),
      ...(collectionRun ?? { id: null }),
    });
    return new FormGroup<CollectionRunFormGroupContent>({
      id: new FormControl(
        { value: collectionRunRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      startedAt: new FormControl(collectionRunRawValue.startedAt, {
        validators: [Validators.required],
      }),
      finishedAt: new FormControl(collectionRunRawValue.finishedAt),
      status: new FormControl(collectionRunRawValue.status, {
        validators: [Validators.required],
      }),
      foundCount: new FormControl(collectionRunRawValue.foundCount),
      processedCount: new FormControl(collectionRunRawValue.processedCount),
      errorMessage: new FormControl(collectionRunRawValue.errorMessage, {
        validators: [Validators.maxLength(4000)],
      }),
    });
  }

  getCollectionRun(form: CollectionRunFormGroup): ICollectionRun | NewCollectionRun {
    return this.convertCollectionRunRawValueToCollectionRun(form.getRawValue());
  }

  resetForm(form: CollectionRunFormGroup, collectionRun: CollectionRunFormGroupInput): void {
    const collectionRunRawValue = this.convertCollectionRunToCollectionRunRawValue({ ...this.getFormDefaults(), ...collectionRun });
    form.reset({
      ...collectionRunRawValue,
      id: { value: collectionRunRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): CollectionRunFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      startedAt: currentTime,
      finishedAt: currentTime,
    };
  }

  private convertCollectionRunRawValueToCollectionRun(
    rawCollectionRun: CollectionRunFormRawValue | NewCollectionRunFormRawValue,
  ): ICollectionRun | NewCollectionRun {
    return {
      ...rawCollectionRun,
      startedAt: dayjs(rawCollectionRun.startedAt, DATE_TIME_FORMAT),
      finishedAt: dayjs(rawCollectionRun.finishedAt, DATE_TIME_FORMAT),
    };
  }

  private convertCollectionRunToCollectionRunRawValue(
    collectionRun: ICollectionRun | (Partial<NewCollectionRun> & CollectionRunFormDefaults),
  ): CollectionRunFormRawValue | PartialWithRequiredKeyOf<NewCollectionRunFormRawValue> {
    return {
      ...collectionRun,
      startedAt: collectionRun.startedAt ? collectionRun.startedAt.format(DATE_TIME_FORMAT) : undefined,
      finishedAt: collectionRun.finishedAt ? collectionRun.finishedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
