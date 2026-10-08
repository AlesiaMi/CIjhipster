import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../data-source.test-samples';

import { DataSourceFormService } from './data-source-form.service';

describe('DataSource Form Service', () => {
  let service: DataSourceFormService;

  beforeEach(() => {
    service = TestBed.inject(DataSourceFormService);
  });

  describe('Service methods', () => {
    describe('createDataSourceFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createDataSourceFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            sourceName: expect.any(Object),
            url: expect.any(Object),
            sourceType: expect.any(Object),
            isActive: expect.any(Object),
            lastCheckedAt: expect.any(Object),
            createdAt: expect.any(Object),
            competitor: expect.any(Object),
          }),
        );
      });

      it('passing IDataSource should create a new form with FormGroup', () => {
        const formGroup = service.createDataSourceFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            sourceName: expect.any(Object),
            url: expect.any(Object),
            sourceType: expect.any(Object),
            isActive: expect.any(Object),
            lastCheckedAt: expect.any(Object),
            createdAt: expect.any(Object),
            competitor: expect.any(Object),
          }),
        );
      });
    });

    describe('getDataSource', () => {
      it('should return NewDataSource for default DataSource initial value', () => {
        const formGroup = service.createDataSourceFormGroup(sampleWithNewData);

        const dataSource = service.getDataSource(formGroup);

        expect(dataSource).toMatchObject(sampleWithNewData);
      });

      it('should return NewDataSource for empty DataSource initial value', () => {
        const formGroup = service.createDataSourceFormGroup();

        const dataSource = service.getDataSource(formGroup);

        expect(dataSource).toMatchObject({});
      });

      it('should return IDataSource', () => {
        const formGroup = service.createDataSourceFormGroup(sampleWithRequiredData);

        const dataSource = service.getDataSource(formGroup);

        expect(dataSource).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IDataSource should not enable id FormControl', () => {
        const formGroup = service.createDataSourceFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewDataSource should disable id FormControl', () => {
        const formGroup = service.createDataSourceFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
