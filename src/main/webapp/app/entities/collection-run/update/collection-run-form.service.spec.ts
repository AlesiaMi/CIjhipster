import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../collection-run.test-samples';

import { CollectionRunFormService } from './collection-run-form.service';

describe('CollectionRun Form Service', () => {
  let service: CollectionRunFormService;

  beforeEach(() => {
    service = TestBed.inject(CollectionRunFormService);
  });

  describe('Service methods', () => {
    describe('createCollectionRunFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createCollectionRunFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            startedAt: expect.any(Object),
            finishedAt: expect.any(Object),
            status: expect.any(Object),
            foundCount: expect.any(Object),
            processedCount: expect.any(Object),
            errorMessage: expect.any(Object),
          }),
        );
      });

      it('passing ICollectionRun should create a new form with FormGroup', () => {
        const formGroup = service.createCollectionRunFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            startedAt: expect.any(Object),
            finishedAt: expect.any(Object),
            status: expect.any(Object),
            foundCount: expect.any(Object),
            processedCount: expect.any(Object),
            errorMessage: expect.any(Object),
          }),
        );
      });
    });

    describe('getCollectionRun', () => {
      it('should return NewCollectionRun for default CollectionRun initial value', () => {
        const formGroup = service.createCollectionRunFormGroup(sampleWithNewData);

        const collectionRun = service.getCollectionRun(formGroup);

        expect(collectionRun).toMatchObject(sampleWithNewData);
      });

      it('should return NewCollectionRun for empty CollectionRun initial value', () => {
        const formGroup = service.createCollectionRunFormGroup();

        const collectionRun = service.getCollectionRun(formGroup);

        expect(collectionRun).toMatchObject({});
      });

      it('should return ICollectionRun', () => {
        const formGroup = service.createCollectionRunFormGroup(sampleWithRequiredData);

        const collectionRun = service.getCollectionRun(formGroup);

        expect(collectionRun).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ICollectionRun should not enable id FormControl', () => {
        const formGroup = service.createCollectionRunFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewCollectionRun should disable id FormControl', () => {
        const formGroup = service.createCollectionRunFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
