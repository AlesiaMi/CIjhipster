import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../analysis-result.test-samples';

import { AnalysisResultFormService } from './analysis-result-form.service';

describe('AnalysisResult Form Service', () => {
  let service: AnalysisResultFormService;

  beforeEach(() => {
    service = TestBed.inject(AnalysisResultFormService);
  });

  describe('Service methods', () => {
    describe('createAnalysisResultFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAnalysisResultFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            summary: expect.any(Object),
            sentiment: expect.any(Object),
            topic: expect.any(Object),
            entities: expect.any(Object),
            riskSource: expect.any(Object),
            status: expect.any(Object),
            modelName: expect.any(Object),
            analyzedAt: expect.any(Object),
            errorMessage: expect.any(Object),
            newsItem: expect.any(Object),
          }),
        );
      });

      it('passing IAnalysisResult should create a new form with FormGroup', () => {
        const formGroup = service.createAnalysisResultFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            summary: expect.any(Object),
            sentiment: expect.any(Object),
            topic: expect.any(Object),
            entities: expect.any(Object),
            riskSource: expect.any(Object),
            status: expect.any(Object),
            modelName: expect.any(Object),
            analyzedAt: expect.any(Object),
            errorMessage: expect.any(Object),
            newsItem: expect.any(Object),
          }),
        );
      });
    });

    describe('getAnalysisResult', () => {
      it('should return NewAnalysisResult for default AnalysisResult initial value', () => {
        const formGroup = service.createAnalysisResultFormGroup(sampleWithNewData);

        const analysisResult = service.getAnalysisResult(formGroup);

        expect(analysisResult).toMatchObject(sampleWithNewData);
      });

      it('should return NewAnalysisResult for empty AnalysisResult initial value', () => {
        const formGroup = service.createAnalysisResultFormGroup();

        const analysisResult = service.getAnalysisResult(formGroup);

        expect(analysisResult).toMatchObject({});
      });

      it('should return IAnalysisResult', () => {
        const formGroup = service.createAnalysisResultFormGroup(sampleWithRequiredData);

        const analysisResult = service.getAnalysisResult(formGroup);

        expect(analysisResult).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAnalysisResult should not enable id FormControl', () => {
        const formGroup = service.createAnalysisResultFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAnalysisResult should disable id FormControl', () => {
        const formGroup = service.createAnalysisResultFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
