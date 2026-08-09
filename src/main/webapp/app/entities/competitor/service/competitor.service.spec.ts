import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ICompetitor } from '../competitor.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../competitor.test-samples';

import { CompetitorService } from './competitor.service';

const requireRestSample: ICompetitor = {
  ...sampleWithRequiredData,
};

describe('Competitor Service', () => {
  let service: CompetitorService;
  let httpMock: HttpTestingController;
  let expectedResult: ICompetitor | ICompetitor[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(CompetitorService);
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

    it('should create a Competitor', () => {
      const competitor = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(competitor).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Competitor', () => {
      const competitor = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(competitor).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Competitor', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Competitor', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Competitor', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addCompetitorToCollectionIfMissing', () => {
      it('should add a Competitor to an empty array', () => {
        const competitor: ICompetitor = sampleWithRequiredData;
        expectedResult = service.addCompetitorToCollectionIfMissing([], competitor);
        expect(expectedResult).toEqual([competitor]);
      });

      it('should not add a Competitor to an array that contains it', () => {
        const competitor: ICompetitor = sampleWithRequiredData;
        const competitorCollection: ICompetitor[] = [
          {
            ...competitor,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addCompetitorToCollectionIfMissing(competitorCollection, competitor);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Competitor to an array that doesn't contain it", () => {
        const competitor: ICompetitor = sampleWithRequiredData;
        const competitorCollection: ICompetitor[] = [sampleWithPartialData];
        expectedResult = service.addCompetitorToCollectionIfMissing(competitorCollection, competitor);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(competitor);
      });

      it('should add only unique Competitor to an array', () => {
        const competitorArray: ICompetitor[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const competitorCollection: ICompetitor[] = [sampleWithRequiredData];
        expectedResult = service.addCompetitorToCollectionIfMissing(competitorCollection, ...competitorArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const competitor: ICompetitor = sampleWithRequiredData;
        const competitor2: ICompetitor = sampleWithPartialData;
        expectedResult = service.addCompetitorToCollectionIfMissing([], competitor, competitor2);
        expect(expectedResult).toEqual([competitor, competitor2]);
      });

      it('should accept null and undefined values', () => {
        const competitor: ICompetitor = sampleWithRequiredData;
        expectedResult = service.addCompetitorToCollectionIfMissing([], null, competitor, undefined);
        expect(expectedResult).toEqual([competitor]);
      });

      it('should return initial array if no Competitor is added', () => {
        const competitorCollection: ICompetitor[] = [sampleWithRequiredData];
        expectedResult = service.addCompetitorToCollectionIfMissing(competitorCollection, undefined, null);
        expect(expectedResult).toEqual(competitorCollection);
      });
    });

    describe('compareCompetitor', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareCompetitor(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 6672 };
        const entity2 = null;

        const compareResult1 = service.compareCompetitor(entity1, entity2);
        const compareResult2 = service.compareCompetitor(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 6672 };
        const entity2 = { id: 15804 };

        const compareResult1 = service.compareCompetitor(entity1, entity2);
        const compareResult2 = service.compareCompetitor(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 6672 };
        const entity2 = { id: 6672 };

        const compareResult1 = service.compareCompetitor(entity1, entity2);
        const compareResult2 = service.compareCompetitor(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
