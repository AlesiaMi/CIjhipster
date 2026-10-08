import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../analyst-profile.test-samples';

import { AnalystProfileFormService } from './analyst-profile-form.service';

describe('AnalystProfile Form Service', () => {
  let service: AnalystProfileFormService;

  beforeEach(() => {
    service = TestBed.inject(AnalystProfileFormService);
  });

  describe('Service methods', () => {
    describe('createAnalystProfileFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAnalystProfileFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            displayName: expect.any(Object),
            telegramChatId: expect.any(Object),
            notificationEnabled: expect.any(Object),
            createdAt: expect.any(Object),
            user: expect.any(Object),
            competitorses: expect.any(Object),
          }),
        );
      });

      it('passing IAnalystProfile should create a new form with FormGroup', () => {
        const formGroup = service.createAnalystProfileFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            displayName: expect.any(Object),
            telegramChatId: expect.any(Object),
            notificationEnabled: expect.any(Object),
            createdAt: expect.any(Object),
            user: expect.any(Object),
            competitorses: expect.any(Object),
          }),
        );
      });
    });

    describe('getAnalystProfile', () => {
      it('should return NewAnalystProfile for default AnalystProfile initial value', () => {
        const formGroup = service.createAnalystProfileFormGroup(sampleWithNewData);

        const analystProfile = service.getAnalystProfile(formGroup);

        expect(analystProfile).toMatchObject(sampleWithNewData);
      });

      it('should return NewAnalystProfile for empty AnalystProfile initial value', () => {
        const formGroup = service.createAnalystProfileFormGroup();

        const analystProfile = service.getAnalystProfile(formGroup);

        expect(analystProfile).toMatchObject({});
      });

      it('should return IAnalystProfile', () => {
        const formGroup = service.createAnalystProfileFormGroup(sampleWithRequiredData);

        const analystProfile = service.getAnalystProfile(formGroup);

        expect(analystProfile).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAnalystProfile should not enable id FormControl', () => {
        const formGroup = service.createAnalystProfileFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAnalystProfile should disable id FormControl', () => {
        const formGroup = service.createAnalystProfileFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
