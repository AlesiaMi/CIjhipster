import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { INewsItem } from '../news-item.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../news-item.test-samples';

import { NewsItemService, RestNewsItem } from './news-item.service';

const requireRestSample: RestNewsItem = {
  ...sampleWithRequiredData,
  publishedAt: sampleWithRequiredData.publishedAt?.toJSON(),
  collectedAt: sampleWithRequiredData.collectedAt?.toJSON(),
};

describe('NewsItem Service', () => {
  let service: NewsItemService;
  let httpMock: HttpTestingController;
  let expectedResult: INewsItem | INewsItem[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(NewsItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a NewsItem', () => {
      const newsItem = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(newsItem).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a NewsItem', () => {
      const newsItem = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(newsItem).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a NewsItem', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of NewsItem', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a NewsItem', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addNewsItemToCollectionIfMissing', () => {
      it('should add a NewsItem to an empty array', () => {
        const newsItem: INewsItem = sampleWithRequiredData;
        expectedResult = service.addNewsItemToCollectionIfMissing([], newsItem);
        expect(expectedResult).toEqual([newsItem]);
      });

      it('should not add a NewsItem to an array that contains it', () => {
        const newsItem: INewsItem = sampleWithRequiredData;
        const newsItemCollection: INewsItem[] = [
          {
            ...newsItem,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addNewsItemToCollectionIfMissing(newsItemCollection, newsItem);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a NewsItem to an array that doesn't contain it", () => {
        const newsItem: INewsItem = sampleWithRequiredData;
        const newsItemCollection: INewsItem[] = [sampleWithPartialData];
        expectedResult = service.addNewsItemToCollectionIfMissing(newsItemCollection, newsItem);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(newsItem);
      });

      it('should add only unique NewsItem to an array', () => {
        const newsItemArray: INewsItem[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const newsItemCollection: INewsItem[] = [sampleWithRequiredData];
        expectedResult = service.addNewsItemToCollectionIfMissing(newsItemCollection, ...newsItemArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const newsItem: INewsItem = sampleWithRequiredData;
        const newsItem2: INewsItem = sampleWithPartialData;
        expectedResult = service.addNewsItemToCollectionIfMissing([], newsItem, newsItem2);
        expect(expectedResult).toEqual([newsItem, newsItem2]);
      });

      it('should accept null and undefined values', () => {
        const newsItem: INewsItem = sampleWithRequiredData;
        expectedResult = service.addNewsItemToCollectionIfMissing([], null, newsItem, undefined);
        expect(expectedResult).toEqual([newsItem]);
      });

      it('should return initial array if no NewsItem is added', () => {
        const newsItemCollection: INewsItem[] = [sampleWithRequiredData];
        expectedResult = service.addNewsItemToCollectionIfMissing(newsItemCollection, undefined, null);
        expect(expectedResult).toEqual(newsItemCollection);
      });
    });

    describe('compareNewsItem', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareNewsItem(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 25999 };
        const entity2 = null;

        const compareResult1 = service.compareNewsItem(entity1, entity2);
        const compareResult2 = service.compareNewsItem(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 25999 };
        const entity2 = { id: 14522 };

        const compareResult1 = service.compareNewsItem(entity1, entity2);
        const compareResult2 = service.compareNewsItem(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 25999 };
        const entity2 = { id: 25999 };

        const compareResult1 = service.compareNewsItem(entity1, entity2);
        const compareResult2 = service.compareNewsItem(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
