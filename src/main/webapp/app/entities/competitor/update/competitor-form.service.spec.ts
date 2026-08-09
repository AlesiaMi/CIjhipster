import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../competitor.test-samples';

import { CompetitorFormService } from './competitor-form.service';

describe('Competitor Form Service', () => {
  let service: CompetitorFormService;

  beforeEach(() => {
    service = TestBed.inject(CompetitorFormService);
  });

  describe('Service methods', () => {
    describe('createCompetitorFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createCompetitorFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            competitorName: expect.any(Object),
            websiteUrl: expect.any(Object),
            industry: expect.any(Object),
            description: expect.any(Object),
            isActive: expect.any(Object),
            analystProfileses: expect.any(Object),
          }),
        );
      });

      it('passing ICompetitor should create a new form with FormGroup', () => {
        const formGroup = service.createCompetitorFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            competitorName: expect.any(Object),
            websiteUrl: expect.any(Object),
            industry: expect.any(Object),
            description: expect.any(Object),
            isActive: expect.any(Object),
            analystProfileses: expect.any(Object),
          }),
        );
      });
    });

    describe('getCompetitor', () => {
      it('should return NewCompetitor for default Competitor initial value', () => {
        const formGroup = service.createCompetitorFormGroup(sampleWithNewData);

        const competitor = service.getCompetitor(formGroup);

        expect(competitor).toMatchObject(sampleWithNewData);
      });

      it('should return NewCompetitor for empty Competitor initial value', () => {
        const formGroup = service.createCompetitorFormGroup();

        const competitor = service.getCompetitor(formGroup);

        expect(competitor).toMatchObject({});
      });

      it('should return ICompetitor', () => {
        const formGroup = service.createCompetitorFormGroup(sampleWithRequiredData);

        const competitor = service.getCompetitor(formGroup);

        expect(competitor).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ICompetitor should not enable id FormControl', () => {
        const formGroup = service.createCompetitorFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewCompetitor should disable id FormControl', () => {
        const formGroup = service.createCompetitorFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
