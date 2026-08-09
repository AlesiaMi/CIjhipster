import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IAnalysisResult } from '../analysis-result.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../analysis-result.test-samples';

import { AnalysisResultService, RestAnalysisResult } from './analysis-result.service';

const requireRestSample: RestAnalysisResult = {
  ...sampleWithRequiredData,
  analyzedAt: sampleWithRequiredData.analyzedAt?.toJSON(),
};

describe('AnalysisResult Service', () => {
  let service: AnalysisResultService;
  let httpMock: HttpTestingController;
  let expectedResult: IAnalysisResult | IAnalysisResult[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(AnalysisResultService);
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

    it('should create a AnalysisResult', () => {
      const analysisResult = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(analysisResult).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AnalysisResult', () => {
      const analysisResult = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(analysisResult).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AnalysisResult', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AnalysisResult', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AnalysisResult', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addAnalysisResultToCollectionIfMissing', () => {
      it('should add a AnalysisResult to an empty array', () => {
        const analysisResult: IAnalysisResult = sampleWithRequiredData;
        expectedResult = service.addAnalysisResultToCollectionIfMissing([], analysisResult);
        expect(expectedResult).toEqual([analysisResult]);
      });

      it('should not add a AnalysisResult to an array that contains it', () => {
        const analysisResult: IAnalysisResult = sampleWithRequiredData;
        const analysisResultCollection: IAnalysisResult[] = [
          {
            ...analysisResult,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAnalysisResultToCollectionIfMissing(analysisResultCollection, analysisResult);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AnalysisResult to an array that doesn't contain it", () => {
        const analysisResult: IAnalysisResult = sampleWithRequiredData;
        const analysisResultCollection: IAnalysisResult[] = [sampleWithPartialData];
        expectedResult = service.addAnalysisResultToCollectionIfMissing(analysisResultCollection, analysisResult);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(analysisResult);
      });

      it('should add only unique AnalysisResult to an array', () => {
        const analysisResultArray: IAnalysisResult[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const analysisResultCollection: IAnalysisResult[] = [sampleWithRequiredData];
        expectedResult = service.addAnalysisResultToCollectionIfMissing(analysisResultCollection, ...analysisResultArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const analysisResult: IAnalysisResult = sampleWithRequiredData;
        const analysisResult2: IAnalysisResult = sampleWithPartialData;
        expectedResult = service.addAnalysisResultToCollectionIfMissing([], analysisResult, analysisResult2);
        expect(expectedResult).toEqual([analysisResult, analysisResult2]);
      });

      it('should accept null and undefined values', () => {
        const analysisResult: IAnalysisResult = sampleWithRequiredData;
        expectedResult = service.addAnalysisResultToCollectionIfMissing([], null, analysisResult, undefined);
        expect(expectedResult).toEqual([analysisResult]);
      });

      it('should return initial array if no AnalysisResult is added', () => {
        const analysisResultCollection: IAnalysisResult[] = [sampleWithRequiredData];
        expectedResult = service.addAnalysisResultToCollectionIfMissing(analysisResultCollection, undefined, null);
        expect(expectedResult).toEqual(analysisResultCollection);
      });
    });

    describe('compareAnalysisResult', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAnalysisResult(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 28488 };
        const entity2 = null;

        const compareResult1 = service.compareAnalysisResult(entity1, entity2);
        const compareResult2 = service.compareAnalysisResult(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 28488 };
        const entity2 = { id: 32267 };

        const compareResult1 = service.compareAnalysisResult(entity1, entity2);
        const compareResult2 = service.compareAnalysisResult(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 28488 };
        const entity2 = { id: 28488 };

        const compareResult1 = service.compareAnalysisResult(entity1, entity2);
        const compareResult2 = service.compareAnalysisResult(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
