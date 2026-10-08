import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAnalysisResult, NewAnalysisResult } from '../analysis-result.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAnalysisResult for edit and NewAnalysisResultFormGroupInput for create.
 */
type AnalysisResultFormGroupInput = IAnalysisResult | PartialWithRequiredKeyOf<NewAnalysisResult>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAnalysisResult | NewAnalysisResult> = Omit<T, 'analyzedAt'> & {
  analyzedAt?: string | null;
};

type AnalysisResultFormRawValue = FormValueOf<IAnalysisResult>;

type NewAnalysisResultFormRawValue = FormValueOf<NewAnalysisResult>;

type AnalysisResultFormDefaults = Pick<NewAnalysisResult, 'id' | 'analyzedAt'>;

type AnalysisResultFormGroupContent = {
  id: FormControl<AnalysisResultFormRawValue['id'] | NewAnalysisResult['id']>;
  summary: FormControl<AnalysisResultFormRawValue['summary']>;
  sentiment: FormControl<AnalysisResultFormRawValue['sentiment']>;
  topic: FormControl<AnalysisResultFormRawValue['topic']>;
  entities: FormControl<AnalysisResultFormRawValue['entities']>;
  riskSource: FormControl<AnalysisResultFormRawValue['riskSource']>;
  status: FormControl<AnalysisResultFormRawValue['status']>;
  modelName: FormControl<AnalysisResultFormRawValue['modelName']>;
  analyzedAt: FormControl<AnalysisResultFormRawValue['analyzedAt']>;
  errorMessage: FormControl<AnalysisResultFormRawValue['errorMessage']>;
  newsItem: FormControl<AnalysisResultFormRawValue['newsItem']>;
};

export type AnalysisResultFormGroup = FormGroup<AnalysisResultFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AnalysisResultFormService {
  createAnalysisResultFormGroup(analysisResult?: AnalysisResultFormGroupInput): AnalysisResultFormGroup {
    const analysisResultRawValue = this.convertAnalysisResultToAnalysisResultRawValue({
      ...this.getFormDefaults(),
      ...(analysisResult ?? { id: null }),
    });
    return new FormGroup<AnalysisResultFormGroupContent>({
      id: new FormControl(
        { value: analysisResultRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      summary: new FormControl(analysisResultRawValue.summary, {
        validators: [Validators.maxLength(4000)],
      }),
      sentiment: new FormControl(analysisResultRawValue.sentiment, {
        validators: [Validators.required],
      }),
      topic: new FormControl(analysisResultRawValue.topic, {
        validators: [Validators.maxLength(255)],
      }),
      entities: new FormControl(analysisResultRawValue.entities, {
        validators: [Validators.maxLength(4000)],
      }),
      riskSource: new FormControl(analysisResultRawValue.riskSource, {
        validators: [Validators.maxLength(255)],
      }),
      status: new FormControl(analysisResultRawValue.status, {
        validators: [Validators.required],
      }),
      modelName: new FormControl(analysisResultRawValue.modelName, {
        validators: [Validators.maxLength(100)],
      }),
      analyzedAt: new FormControl(analysisResultRawValue.analyzedAt),
      errorMessage: new FormControl(analysisResultRawValue.errorMessage, {
        validators: [Validators.maxLength(4000)],
      }),
      newsItem: new FormControl(analysisResultRawValue.newsItem, {
        validators: [Validators.required],
      }),
    });
  }

  getAnalysisResult(form: AnalysisResultFormGroup): IAnalysisResult | NewAnalysisResult {
    return this.convertAnalysisResultRawValueToAnalysisResult(form.getRawValue());
  }

  resetForm(form: AnalysisResultFormGroup, analysisResult: AnalysisResultFormGroupInput): void {
    const analysisResultRawValue = this.convertAnalysisResultToAnalysisResultRawValue({ ...this.getFormDefaults(), ...analysisResult });
    form.reset({
      ...analysisResultRawValue,
      id: { value: analysisResultRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): AnalysisResultFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      analyzedAt: currentTime,
    };
  }

  private convertAnalysisResultRawValueToAnalysisResult(
    rawAnalysisResult: AnalysisResultFormRawValue | NewAnalysisResultFormRawValue,
  ): IAnalysisResult | NewAnalysisResult {
    return {
      ...rawAnalysisResult,
      analyzedAt: dayjs(rawAnalysisResult.analyzedAt, DATE_TIME_FORMAT),
    };
  }

  private convertAnalysisResultToAnalysisResultRawValue(
    analysisResult: IAnalysisResult | (Partial<NewAnalysisResult> & AnalysisResultFormDefaults),
  ): AnalysisResultFormRawValue | PartialWithRequiredKeyOf<NewAnalysisResultFormRawValue> {
    return {
      ...analysisResult,
      analyzedAt: analysisResult.analyzedAt ? analysisResult.analyzedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
