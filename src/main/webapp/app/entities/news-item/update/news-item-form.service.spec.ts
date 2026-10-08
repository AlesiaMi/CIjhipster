import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../news-item.test-samples';

import { NewsItemFormService } from './news-item-form.service';

describe('NewsItem Form Service', () => {
  let service: NewsItemFormService;

  beforeEach(() => {
    service = TestBed.inject(NewsItemFormService);
  });

  describe('Service methods', () => {
    describe('createNewsItemFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createNewsItemFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            externalId: expect.any(Object),
            title: expect.any(Object),
            url: expect.any(Object),
            originalText: expect.any(Object),
            publishedAt: expect.any(Object),
            collectedAt: expect.any(Object),
            isDuplicate: expect.any(Object),
            dataSource: expect.any(Object),
            competitor: expect.any(Object),
            collectionRun: expect.any(Object),
          }),
        );
      });

      it('passing INewsItem should create a new form with FormGroup', () => {
        const formGroup = service.createNewsItemFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            externalId: expect.any(Object),
            title: expect.any(Object),
            url: expect.any(Object),
            originalText: expect.any(Object),
            publishedAt: expect.any(Object),
            collectedAt: expect.any(Object),
            isDuplicate: expect.any(Object),
            dataSource: expect.any(Object),
            competitor: expect.any(Object),
            collectionRun: expect.any(Object),
          }),
        );
      });
    });

    describe('getNewsItem', () => {
      it('should return NewNewsItem for default NewsItem initial value', () => {
        const formGroup = service.createNewsItemFormGroup(sampleWithNewData);

        const newsItem = service.getNewsItem(formGroup);

        expect(newsItem).toMatchObject(sampleWithNewData);
      });

      it('should return NewNewsItem for empty NewsItem initial value', () => {
        const formGroup = service.createNewsItemFormGroup();

        const newsItem = service.getNewsItem(formGroup);

        expect(newsItem).toMatchObject({});
      });

      it('should return INewsItem', () => {
        const formGroup = service.createNewsItemFormGroup(sampleWithRequiredData);

        const newsItem = service.getNewsItem(formGroup);

        expect(newsItem).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing INewsItem should not enable id FormControl', () => {
        const formGroup = service.createNewsItemFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewNewsItem should disable id FormControl', () => {
        const formGroup = service.createNewsItemFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
