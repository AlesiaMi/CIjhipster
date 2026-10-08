import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../ci-alert.test-samples';

import { CiAlertFormService } from './ci-alert-form.service';

describe('CiAlert Form Service', () => {
  let service: CiAlertFormService;

  beforeEach(() => {
    service = TestBed.inject(CiAlertFormService);
  });

  describe('Service methods', () => {
    describe('createCiAlertFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createCiAlertFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            message: expect.any(Object),
            severity: expect.any(Object),
            status: expect.any(Object),
            createdAt: expect.any(Object),
            readAt: expect.any(Object),
            analysisResult: expect.any(Object),
            analystProfile: expect.any(Object),
          }),
        );
      });

      it('passing ICiAlert should create a new form with FormGroup', () => {
        const formGroup = service.createCiAlertFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            message: expect.any(Object),
            severity: expect.any(Object),
            status: expect.any(Object),
            createdAt: expect.any(Object),
            readAt: expect.any(Object),
            analysisResult: expect.any(Object),
            analystProfile: expect.any(Object),
          }),
        );
      });
    });

    describe('getCiAlert', () => {
      it('should return NewCiAlert for default CiAlert initial value', () => {
        const formGroup = service.createCiAlertFormGroup(sampleWithNewData);

        const ciAlert = service.getCiAlert(formGroup);

        expect(ciAlert).toMatchObject(sampleWithNewData);
      });

      it('should return NewCiAlert for empty CiAlert initial value', () => {
        const formGroup = service.createCiAlertFormGroup();

        const ciAlert = service.getCiAlert(formGroup);

        expect(ciAlert).toMatchObject({});
      });

      it('should return ICiAlert', () => {
        const formGroup = service.createCiAlertFormGroup(sampleWithRequiredData);

        const ciAlert = service.getCiAlert(formGroup);

        expect(ciAlert).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ICiAlert should not enable id FormControl', () => {
        const formGroup = service.createCiAlertFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewCiAlert should disable id FormControl', () => {
        const formGroup = service.createCiAlertFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
