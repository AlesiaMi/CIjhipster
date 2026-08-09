import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IAnalystProfile } from '../analyst-profile.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../analyst-profile.test-samples';

import { AnalystProfileService, RestAnalystProfile } from './analyst-profile.service';

const requireRestSample: RestAnalystProfile = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
};

describe('AnalystProfile Service', () => {
  let service: AnalystProfileService;
  let httpMock: HttpTestingController;
  let expectedResult: IAnalystProfile | IAnalystProfile[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(AnalystProfileService);
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

    it('should create a AnalystProfile', () => {
      const analystProfile = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(analystProfile).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AnalystProfile', () => {
      const analystProfile = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(analystProfile).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AnalystProfile', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AnalystProfile', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AnalystProfile', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addAnalystProfileToCollectionIfMissing', () => {
      it('should add a AnalystProfile to an empty array', () => {
        const analystProfile: IAnalystProfile = sampleWithRequiredData;
        expectedResult = service.addAnalystProfileToCollectionIfMissing([], analystProfile);
        expect(expectedResult).toEqual([analystProfile]);
      });

      it('should not add a AnalystProfile to an array that contains it', () => {
        const analystProfile: IAnalystProfile = sampleWithRequiredData;
        const analystProfileCollection: IAnalystProfile[] = [
          {
            ...analystProfile,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAnalystProfileToCollectionIfMissing(analystProfileCollection, analystProfile);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AnalystProfile to an array that doesn't contain it", () => {
        const analystProfile: IAnalystProfile = sampleWithRequiredData;
        const analystProfileCollection: IAnalystProfile[] = [sampleWithPartialData];
        expectedResult = service.addAnalystProfileToCollectionIfMissing(analystProfileCollection, analystProfile);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(analystProfile);
      });

      it('should add only unique AnalystProfile to an array', () => {
        const analystProfileArray: IAnalystProfile[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const analystProfileCollection: IAnalystProfile[] = [sampleWithRequiredData];
        expectedResult = service.addAnalystProfileToCollectionIfMissing(analystProfileCollection, ...analystProfileArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const analystProfile: IAnalystProfile = sampleWithRequiredData;
        const analystProfile2: IAnalystProfile = sampleWithPartialData;
        expectedResult = service.addAnalystProfileToCollectionIfMissing([], analystProfile, analystProfile2);
        expect(expectedResult).toEqual([analystProfile, analystProfile2]);
      });

      it('should accept null and undefined values', () => {
        const analystProfile: IAnalystProfile = sampleWithRequiredData;
        expectedResult = service.addAnalystProfileToCollectionIfMissing([], null, analystProfile, undefined);
        expect(expectedResult).toEqual([analystProfile]);
      });

      it('should return initial array if no AnalystProfile is added', () => {
        const analystProfileCollection: IAnalystProfile[] = [sampleWithRequiredData];
        expectedResult = service.addAnalystProfileToCollectionIfMissing(analystProfileCollection, undefined, null);
        expect(expectedResult).toEqual(analystProfileCollection);
      });
    });

    describe('compareAnalystProfile', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAnalystProfile(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 27303 };
        const entity2 = null;

        const compareResult1 = service.compareAnalystProfile(entity1, entity2);
        const compareResult2 = service.compareAnalystProfile(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 27303 };
        const entity2 = { id: 15537 };

        const compareResult1 = service.compareAnalystProfile(entity1, entity2);
        const compareResult2 = service.compareAnalystProfile(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 27303 };
        const entity2 = { id: 27303 };

        const compareResult1 = service.compareAnalystProfile(entity1, entity2);
        const compareResult2 = service.compareAnalystProfile(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
