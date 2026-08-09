import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ICompetitor, NewCompetitor } from '../competitor.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICompetitor for edit and NewCompetitorFormGroupInput for create.
 */
type CompetitorFormGroupInput = ICompetitor | PartialWithRequiredKeyOf<NewCompetitor>;

type CompetitorFormDefaults = Pick<NewCompetitor, 'id' | 'isActive' | 'analystProfileses'>;

type CompetitorFormGroupContent = {
  id: FormControl<ICompetitor['id'] | NewCompetitor['id']>;
  competitorName: FormControl<ICompetitor['competitorName']>;
  websiteUrl: FormControl<ICompetitor['websiteUrl']>;
  industry: FormControl<ICompetitor['industry']>;
  description: FormControl<ICompetitor['description']>;
  isActive: FormControl<ICompetitor['isActive']>;
  analystProfileses: FormControl<ICompetitor['analystProfileses']>;
};

export type CompetitorFormGroup = FormGroup<CompetitorFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CompetitorFormService {
  createCompetitorFormGroup(competitor?: CompetitorFormGroupInput): CompetitorFormGroup {
    const competitorRawValue = {
      ...this.getFormDefaults(),
      ...(competitor ?? { id: null }),
    };
    return new FormGroup<CompetitorFormGroupContent>({
      id: new FormControl(
        { value: competitorRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      competitorName: new FormControl(competitorRawValue.competitorName, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      websiteUrl: new FormControl(competitorRawValue.websiteUrl, {
        validators: [Validators.maxLength(500)],
      }),
      industry: new FormControl(competitorRawValue.industry, {
        validators: [Validators.maxLength(255)],
      }),
      description: new FormControl(competitorRawValue.description, {
        validators: [Validators.maxLength(2000)],
      }),
      isActive: new FormControl(competitorRawValue.isActive, {
        validators: [Validators.required],
      }),
      analystProfileses: new FormControl(competitorRawValue.analystProfileses ?? []),
    });
  }

  getCompetitor(form: CompetitorFormGroup): ICompetitor | NewCompetitor {
    return form.getRawValue();
  }

  resetForm(form: CompetitorFormGroup, competitor: CompetitorFormGroupInput): void {
    const competitorRawValue = { ...this.getFormDefaults(), ...competitor };
    form.reset({
      ...competitorRawValue,
      id: { value: competitorRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): CompetitorFormDefaults {
    return {
      id: null,
      isActive: false,
      analystProfileses: [],
    };
  }
}
