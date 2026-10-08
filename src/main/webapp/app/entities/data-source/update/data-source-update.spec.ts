import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { IDataSource } from '../data-source.model';
import { DataSourceService } from '../service/data-source.service';

import { DataSourceFormService } from './data-source-form.service';
import { DataSourceUpdate } from './data-source-update';

describe('DataSource Management Update Component', () => {
  let comp: DataSourceUpdate;
  let fixture: ComponentFixture<DataSourceUpdate>;
  let activatedRoute: ActivatedRoute;
  let dataSourceFormService: DataSourceFormService;
  let dataSourceService: DataSourceService;
  let competitorService: CompetitorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(DataSourceUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    dataSourceFormService = TestBed.inject(DataSourceFormService);
    dataSourceService = TestBed.inject(DataSourceService);
    competitorService = TestBed.inject(CompetitorService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Competitor query and add missing value', () => {
      const dataSource: IDataSource = { id: 2244 };
      const competitor: ICompetitor = { id: 6672 };
      dataSource.competitor = competitor;

      const competitorCollection: ICompetitor[] = [{ id: 6672 }];
      vitest.spyOn(competitorService, 'query').mockReturnValue(of(new HttpResponse({ body: competitorCollection })));
      const additionalCompetitors = [competitor];
      const expectedCollection: ICompetitor[] = [...additionalCompetitors, ...competitorCollection];
      vitest.spyOn(competitorService, 'addCompetitorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ dataSource });
      comp.ngOnInit();

      expect(competitorService.query).toHaveBeenCalled();
      expect(competitorService.addCompetitorToCollectionIfMissing).toHaveBeenCalledWith(
        competitorCollection,
        ...additionalCompetitors.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.competitorsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const dataSource: IDataSource = { id: 2244 };
      const competitor: ICompetitor = { id: 6672 };
      dataSource.competitor = competitor;

      activatedRoute.data = of({ dataSource });
      comp.ngOnInit();

      expect(comp.competitorsSharedCollection()).toContainEqual(competitor);
      expect(comp.dataSource).toEqual(dataSource);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IDataSource>();
      const dataSource = { id: 1785 };
      vitest.spyOn(dataSourceFormService, 'getDataSource').mockReturnValue(dataSource);
      vitest.spyOn(dataSourceService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ dataSource });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(dataSource);
      saveSubject.complete();

      // THEN
      expect(dataSourceFormService.getDataSource).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(dataSourceService.update).toHaveBeenCalledWith(expect.objectContaining(dataSource));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IDataSource>();
      const dataSource = { id: 1785 };
      vitest.spyOn(dataSourceFormService, 'getDataSource').mockReturnValue({ id: null });
      vitest.spyOn(dataSourceService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ dataSource: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(dataSource);
      saveSubject.complete();

      // THEN
      expect(dataSourceFormService.getDataSource).toHaveBeenCalled();
      expect(dataSourceService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IDataSource>();
      const dataSource = { id: 1785 };
      vitest.spyOn(dataSourceService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ dataSource });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(dataSourceService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareCompetitor', () => {
      it('should forward to competitorService', () => {
        const entity = { id: 6672 };
        const entity2 = { id: 15804 };
        vitest.spyOn(competitorService, 'compareCompetitor');
        comp.compareCompetitor(entity, entity2);
        expect(competitorService.compareCompetitor).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
