import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ICollectionRun } from '../collection-run.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../collection-run.test-samples';

import { CollectionRunService, RestCollectionRun } from './collection-run.service';

const requireRestSample: RestCollectionRun = {
  ...sampleWithRequiredData,
  startedAt: sampleWithRequiredData.startedAt?.toJSON(),
  finishedAt: sampleWithRequiredData.finishedAt?.toJSON(),
};

describe('CollectionRun Service', () => {
  let service: CollectionRunService;
  let httpMock: HttpTestingController;
  let expectedResult: ICollectionRun | ICollectionRun[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(CollectionRunService);
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

    it('should create a CollectionRun', () => {
      const collectionRun = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(collectionRun).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a CollectionRun', () => {
      const collectionRun = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(collectionRun).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a CollectionRun', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of CollectionRun', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a CollectionRun', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addCollectionRunToCollectionIfMissing', () => {
      it('should add a CollectionRun to an empty array', () => {
        const collectionRun: ICollectionRun = sampleWithRequiredData;
        expectedResult = service.addCollectionRunToCollectionIfMissing([], collectionRun);
        expect(expectedResult).toEqual([collectionRun]);
      });

      it('should not add a CollectionRun to an array that contains it', () => {
        const collectionRun: ICollectionRun = sampleWithRequiredData;
        const collectionRunCollection: ICollectionRun[] = [
          {
            ...collectionRun,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addCollectionRunToCollectionIfMissing(collectionRunCollection, collectionRun);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a CollectionRun to an array that doesn't contain it", () => {
        const collectionRun: ICollectionRun = sampleWithRequiredData;
        const collectionRunCollection: ICollectionRun[] = [sampleWithPartialData];
        expectedResult = service.addCollectionRunToCollectionIfMissing(collectionRunCollection, collectionRun);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(collectionRun);
      });

      it('should add only unique CollectionRun to an array', () => {
        const collectionRunArray: ICollectionRun[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const collectionRunCollection: ICollectionRun[] = [sampleWithRequiredData];
        expectedResult = service.addCollectionRunToCollectionIfMissing(collectionRunCollection, ...collectionRunArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const collectionRun: ICollectionRun = sampleWithRequiredData;
        const collectionRun2: ICollectionRun = sampleWithPartialData;
        expectedResult = service.addCollectionRunToCollectionIfMissing([], collectionRun, collectionRun2);
        expect(expectedResult).toEqual([collectionRun, collectionRun2]);
      });

      it('should accept null and undefined values', () => {
        const collectionRun: ICollectionRun = sampleWithRequiredData;
        expectedResult = service.addCollectionRunToCollectionIfMissing([], null, collectionRun, undefined);
        expect(expectedResult).toEqual([collectionRun]);
      });

      it('should return initial array if no CollectionRun is added', () => {
        const collectionRunCollection: ICollectionRun[] = [sampleWithRequiredData];
        expectedResult = service.addCollectionRunToCollectionIfMissing(collectionRunCollection, undefined, null);
        expect(expectedResult).toEqual(collectionRunCollection);
      });
    });

    describe('compareCollectionRun', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareCollectionRun(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 26304 };
        const entity2 = null;

        const compareResult1 = service.compareCollectionRun(entity1, entity2);
        const compareResult2 = service.compareCollectionRun(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 26304 };
        const entity2 = { id: 22231 };

        const compareResult1 = service.compareCollectionRun(entity1, entity2);
        const compareResult2 = service.compareCollectionRun(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 26304 };
        const entity2 = { id: 26304 };

        const compareResult1 = service.compareCollectionRun(entity1, entity2);
        const compareResult2 = service.compareCollectionRun(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
