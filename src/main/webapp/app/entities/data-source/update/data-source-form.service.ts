import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IDataSource, NewDataSource } from '../data-source.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IDataSource for edit and NewDataSourceFormGroupInput for create.
 */
type DataSourceFormGroupInput = IDataSource | PartialWithRequiredKeyOf<NewDataSource>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IDataSource | NewDataSource> = Omit<T, 'lastCheckedAt' | 'createdAt'> & {
  lastCheckedAt?: string | null;
  createdAt?: string | null;
};

type DataSourceFormRawValue = FormValueOf<IDataSource>;

type NewDataSourceFormRawValue = FormValueOf<NewDataSource>;

type DataSourceFormDefaults = Pick<NewDataSource, 'id' | 'isActive' | 'lastCheckedAt' | 'createdAt'>;

type DataSourceFormGroupContent = {
  id: FormControl<DataSourceFormRawValue['id'] | NewDataSource['id']>;
  sourceName: FormControl<DataSourceFormRawValue['sourceName']>;
  url: FormControl<DataSourceFormRawValue['url']>;
  sourceType: FormControl<DataSourceFormRawValue['sourceType']>;
  isActive: FormControl<DataSourceFormRawValue['isActive']>;
  lastCheckedAt: FormControl<DataSourceFormRawValue['lastCheckedAt']>;
  createdAt: FormControl<DataSourceFormRawValue['createdAt']>;
  competitor: FormControl<DataSourceFormRawValue['competitor']>;
};

export type DataSourceFormGroup = FormGroup<DataSourceFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class DataSourceFormService {
  createDataSourceFormGroup(dataSource?: DataSourceFormGroupInput): DataSourceFormGroup {
    const dataSourceRawValue = this.convertDataSourceToDataSourceRawValue({
      ...this.getFormDefaults(),
      ...(dataSource ?? { id: null }),
    });
    return new FormGroup<DataSourceFormGroupContent>({
      id: new FormControl(
        { value: dataSourceRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      sourceName: new FormControl(dataSourceRawValue.sourceName, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      url: new FormControl(dataSourceRawValue.url, {
        validators: [Validators.required, Validators.maxLength(1000)],
      }),
      sourceType: new FormControl(dataSourceRawValue.sourceType, {
        validators: [Validators.required],
      }),
      isActive: new FormControl(dataSourceRawValue.isActive, {
        validators: [Validators.required],
      }),
      lastCheckedAt: new FormControl(dataSourceRawValue.lastCheckedAt),
      createdAt: new FormControl(dataSourceRawValue.createdAt, {
        validators: [Validators.required],
      }),
      competitor: new FormControl(dataSourceRawValue.competitor, {
        validators: [Validators.required],
      }),
    });
  }

  getDataSource(form: DataSourceFormGroup): IDataSource | NewDataSource {
    return this.convertDataSourceRawValueToDataSource(form.getRawValue());
  }

  resetForm(form: DataSourceFormGroup, dataSource: DataSourceFormGroupInput): void {
    const dataSourceRawValue = this.convertDataSourceToDataSourceRawValue({ ...this.getFormDefaults(), ...dataSource });
    form.reset({
      ...dataSourceRawValue,
      id: { value: dataSourceRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): DataSourceFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      isActive: false,
      lastCheckedAt: currentTime,
      createdAt: currentTime,
    };
  }

  private convertDataSourceRawValueToDataSource(
    rawDataSource: DataSourceFormRawValue | NewDataSourceFormRawValue,
  ): IDataSource | NewDataSource {
    return {
      ...rawDataSource,
      lastCheckedAt: dayjs(rawDataSource.lastCheckedAt, DATE_TIME_FORMAT),
      createdAt: dayjs(rawDataSource.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertDataSourceToDataSourceRawValue(
    dataSource: IDataSource | (Partial<NewDataSource> & DataSourceFormDefaults),
  ): DataSourceFormRawValue | PartialWithRequiredKeyOf<NewDataSourceFormRawValue> {
    return {
      ...dataSource,
      lastCheckedAt: dataSource.lastCheckedAt ? dataSource.lastCheckedAt.format(DATE_TIME_FORMAT) : undefined,
      createdAt: dataSource.createdAt ? dataSource.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
