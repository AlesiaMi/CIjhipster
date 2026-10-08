import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IDataSource } from '../data-source.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../data-source.test-samples';

import { DataSourceService, RestDataSource } from './data-source.service';

const requireRestSample: RestDataSource = {
  ...sampleWithRequiredData,
  lastCheckedAt: sampleWithRequiredData.lastCheckedAt?.toJSON(),
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
};

describe('DataSource Service', () => {
  let service: DataSourceService;
  let httpMock: HttpTestingController;
  let expectedResult: IDataSource | IDataSource[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(DataSourceService);
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

    it('should create a DataSource', () => {
      const dataSource = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(dataSource).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a DataSource', () => {
      const dataSource = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(dataSource).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a DataSource', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of DataSource', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a DataSource', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addDataSourceToCollectionIfMissing', () => {
      it('should add a DataSource to an empty array', () => {
        const dataSource: IDataSource = sampleWithRequiredData;
        expectedResult = service.addDataSourceToCollectionIfMissing([], dataSource);
        expect(expectedResult).toEqual([dataSource]);
      });

      it('should not add a DataSource to an array that contains it', () => {
        const dataSource: IDataSource = sampleWithRequiredData;
        const dataSourceCollection: IDataSource[] = [
          {
            ...dataSource,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addDataSourceToCollectionIfMissing(dataSourceCollection, dataSource);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a DataSource to an array that doesn't contain it", () => {
        const dataSource: IDataSource = sampleWithRequiredData;
        const dataSourceCollection: IDataSource[] = [sampleWithPartialData];
        expectedResult = service.addDataSourceToCollectionIfMissing(dataSourceCollection, dataSource);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(dataSource);
      });

      it('should add only unique DataSource to an array', () => {
        const dataSourceArray: IDataSource[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const dataSourceCollection: IDataSource[] = [sampleWithRequiredData];
        expectedResult = service.addDataSourceToCollectionIfMissing(dataSourceCollection, ...dataSourceArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const dataSource: IDataSource = sampleWithRequiredData;
        const dataSource2: IDataSource = sampleWithPartialData;
        expectedResult = service.addDataSourceToCollectionIfMissing([], dataSource, dataSource2);
        expect(expectedResult).toEqual([dataSource, dataSource2]);
      });

      it('should accept null and undefined values', () => {
        const dataSource: IDataSource = sampleWithRequiredData;
        expectedResult = service.addDataSourceToCollectionIfMissing([], null, dataSource, undefined);
        expect(expectedResult).toEqual([dataSource]);
      });

      it('should return initial array if no DataSource is added', () => {
        const dataSourceCollection: IDataSource[] = [sampleWithRequiredData];
        expectedResult = service.addDataSourceToCollectionIfMissing(dataSourceCollection, undefined, null);
        expect(expectedResult).toEqual(dataSourceCollection);
      });
    });

    describe('compareDataSource', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareDataSource(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 1785 };
        const entity2 = null;

        const compareResult1 = service.compareDataSource(entity1, entity2);
        const compareResult2 = service.compareDataSource(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 1785 };
        const entity2 = { id: 2244 };

        const compareResult1 = service.compareDataSource(entity1, entity2);
        const compareResult2 = service.compareDataSource(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 1785 };
        const entity2 = { id: 1785 };

        const compareResult1 = service.compareDataSource(entity1, entity2);
        const compareResult2 = service.compareDataSource(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
