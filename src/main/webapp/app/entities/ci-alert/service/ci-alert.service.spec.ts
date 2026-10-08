import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ICiAlert } from '../ci-alert.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../ci-alert.test-samples';

import { CiAlertService, RestCiAlert } from './ci-alert.service';

const requireRestSample: RestCiAlert = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
  readAt: sampleWithRequiredData.readAt?.toJSON(),
};

describe('CiAlert Service', () => {
  let service: CiAlertService;
  let httpMock: HttpTestingController;
  let expectedResult: ICiAlert | ICiAlert[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(CiAlertService);
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

    it('should create a CiAlert', () => {
      const ciAlert = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(ciAlert).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a CiAlert', () => {
      const ciAlert = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(ciAlert).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a CiAlert', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of CiAlert', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a CiAlert', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addCiAlertToCollectionIfMissing', () => {
      it('should add a CiAlert to an empty array', () => {
        const ciAlert: ICiAlert = sampleWithRequiredData;
        expectedResult = service.addCiAlertToCollectionIfMissing([], ciAlert);
        expect(expectedResult).toEqual([ciAlert]);
      });

      it('should not add a CiAlert to an array that contains it', () => {
        const ciAlert: ICiAlert = sampleWithRequiredData;
        const ciAlertCollection: ICiAlert[] = [
          {
            ...ciAlert,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addCiAlertToCollectionIfMissing(ciAlertCollection, ciAlert);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a CiAlert to an array that doesn't contain it", () => {
        const ciAlert: ICiAlert = sampleWithRequiredData;
        const ciAlertCollection: ICiAlert[] = [sampleWithPartialData];
        expectedResult = service.addCiAlertToCollectionIfMissing(ciAlertCollection, ciAlert);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(ciAlert);
      });

      it('should add only unique CiAlert to an array', () => {
        const ciAlertArray: ICiAlert[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const ciAlertCollection: ICiAlert[] = [sampleWithRequiredData];
        expectedResult = service.addCiAlertToCollectionIfMissing(ciAlertCollection, ...ciAlertArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const ciAlert: ICiAlert = sampleWithRequiredData;
        const ciAlert2: ICiAlert = sampleWithPartialData;
        expectedResult = service.addCiAlertToCollectionIfMissing([], ciAlert, ciAlert2);
        expect(expectedResult).toEqual([ciAlert, ciAlert2]);
      });

      it('should accept null and undefined values', () => {
        const ciAlert: ICiAlert = sampleWithRequiredData;
        expectedResult = service.addCiAlertToCollectionIfMissing([], null, ciAlert, undefined);
        expect(expectedResult).toEqual([ciAlert]);
      });

      it('should return initial array if no CiAlert is added', () => {
        const ciAlertCollection: ICiAlert[] = [sampleWithRequiredData];
        expectedResult = service.addCiAlertToCollectionIfMissing(ciAlertCollection, undefined, null);
        expect(expectedResult).toEqual(ciAlertCollection);
      });
    });

    describe('compareCiAlert', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareCiAlert(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 1978 };
        const entity2 = null;

        const compareResult1 = service.compareCiAlert(entity1, entity2);
        const compareResult2 = service.compareCiAlert(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 1978 };
        const entity2 = { id: 24603 };

        const compareResult1 = service.compareCiAlert(entity1, entity2);
        const compareResult2 = service.compareCiAlert(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 1978 };
        const entity2 = { id: 1978 };

        const compareResult1 = service.compareCiAlert(entity1, entity2);
        const compareResult2 = service.compareCiAlert(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
