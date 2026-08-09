import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ICollectionRun } from 'app/entities/collection-run/collection-run.model';
import { CollectionRunService } from 'app/entities/collection-run/service/collection-run.service';
import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { IDataSource } from 'app/entities/data-source/data-source.model';
import { DataSourceService } from 'app/entities/data-source/service/data-source.service';
import { INewsItem } from '../news-item.model';
import { NewsItemService } from '../service/news-item.service';

import { NewsItemFormService } from './news-item-form.service';
import { NewsItemUpdate } from './news-item-update';

describe('NewsItem Management Update Component', () => {
  let comp: NewsItemUpdate;
  let fixture: ComponentFixture<NewsItemUpdate>;
  let activatedRoute: ActivatedRoute;
  let newsItemFormService: NewsItemFormService;
  let newsItemService: NewsItemService;
  let dataSourceService: DataSourceService;
  let competitorService: CompetitorService;
  let collectionRunService: CollectionRunService;

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

    fixture = TestBed.createComponent(NewsItemUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    newsItemFormService = TestBed.inject(NewsItemFormService);
    newsItemService = TestBed.inject(NewsItemService);
    dataSourceService = TestBed.inject(DataSourceService);
    competitorService = TestBed.inject(CompetitorService);
    collectionRunService = TestBed.inject(CollectionRunService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call DataSource query and add missing value', () => {
      const newsItem: INewsItem = { id: 14522 };
      const dataSource: IDataSource = { id: 1785 };
      newsItem.dataSource = dataSource;

      const dataSourceCollection: IDataSource[] = [{ id: 1785 }];
      vitest.spyOn(dataSourceService, 'query').mockReturnValue(of(new HttpResponse({ body: dataSourceCollection })));
      const additionalDataSources = [dataSource];
      const expectedCollection: IDataSource[] = [...additionalDataSources, ...dataSourceCollection];
      vitest.spyOn(dataSourceService, 'addDataSourceToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ newsItem });
      comp.ngOnInit();

      expect(dataSourceService.query).toHaveBeenCalled();
      expect(dataSourceService.addDataSourceToCollectionIfMissing).toHaveBeenCalledWith(
        dataSourceCollection,
        ...additionalDataSources.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.dataSourcesSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Competitor query and add missing value', () => {
      const newsItem: INewsItem = { id: 14522 };
      const competitor: ICompetitor = { id: 6672 };
      newsItem.competitor = competitor;

      const competitorCollection: ICompetitor[] = [{ id: 6672 }];
      vitest.spyOn(competitorService, 'query').mockReturnValue(of(new HttpResponse({ body: competitorCollection })));
      const additionalCompetitors = [competitor];
      const expectedCollection: ICompetitor[] = [...additionalCompetitors, ...competitorCollection];
      vitest.spyOn(competitorService, 'addCompetitorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ newsItem });
      comp.ngOnInit();

      expect(competitorService.query).toHaveBeenCalled();
      expect(competitorService.addCompetitorToCollectionIfMissing).toHaveBeenCalledWith(
        competitorCollection,
        ...additionalCompetitors.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.competitorsSharedCollection()).toEqual(expectedCollection);
    });

    it('should call CollectionRun query and add missing value', () => {
      const newsItem: INewsItem = { id: 14522 };
      const collectionRun: ICollectionRun = { id: 26304 };
      newsItem.collectionRun = collectionRun;

      const collectionRunCollection: ICollectionRun[] = [{ id: 26304 }];
      vitest.spyOn(collectionRunService, 'query').mockReturnValue(of(new HttpResponse({ body: collectionRunCollection })));
      const additionalCollectionRuns = [collectionRun];
      const expectedCollection: ICollectionRun[] = [...additionalCollectionRuns, ...collectionRunCollection];
      vitest.spyOn(collectionRunService, 'addCollectionRunToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ newsItem });
      comp.ngOnInit();

      expect(collectionRunService.query).toHaveBeenCalled();
      expect(collectionRunService.addCollectionRunToCollectionIfMissing).toHaveBeenCalledWith(
        collectionRunCollection,
        ...additionalCollectionRuns.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.collectionRunsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const newsItem: INewsItem = { id: 14522 };
      const dataSource: IDataSource = { id: 1785 };
      newsItem.dataSource = dataSource;
      const competitor: ICompetitor = { id: 6672 };
      newsItem.competitor = competitor;
      const collectionRun: ICollectionRun = { id: 26304 };
      newsItem.collectionRun = collectionRun;

      activatedRoute.data = of({ newsItem });
      comp.ngOnInit();

      expect(comp.dataSourcesSharedCollection()).toContainEqual(dataSource);
      expect(comp.competitorsSharedCollection()).toContainEqual(competitor);
      expect(comp.collectionRunsSharedCollection()).toContainEqual(collectionRun);
      expect(comp.newsItem).toEqual(newsItem);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<INewsItem>();
      const newsItem = { id: 25999 };
      vitest.spyOn(newsItemFormService, 'getNewsItem').mockReturnValue(newsItem);
      vitest.spyOn(newsItemService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ newsItem });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(newsItem);
      saveSubject.complete();

      // THEN
      expect(newsItemFormService.getNewsItem).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(newsItemService.update).toHaveBeenCalledWith(expect.objectContaining(newsItem));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<INewsItem>();
      const newsItem = { id: 25999 };
      vitest.spyOn(newsItemFormService, 'getNewsItem').mockReturnValue({ id: null });
      vitest.spyOn(newsItemService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ newsItem: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(newsItem);
      saveSubject.complete();

      // THEN
      expect(newsItemFormService.getNewsItem).toHaveBeenCalled();
      expect(newsItemService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<INewsItem>();
      const newsItem = { id: 25999 };
      vitest.spyOn(newsItemService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ newsItem });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(newsItemService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareDataSource', () => {
      it('should forward to dataSourceService', () => {
        const entity = { id: 1785 };
        const entity2 = { id: 2244 };
        vitest.spyOn(dataSourceService, 'compareDataSource');
        comp.compareDataSource(entity, entity2);
        expect(dataSourceService.compareDataSource).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareCompetitor', () => {
      it('should forward to competitorService', () => {
        const entity = { id: 6672 };
        const entity2 = { id: 15804 };
        vitest.spyOn(competitorService, 'compareCompetitor');
        comp.compareCompetitor(entity, entity2);
        expect(competitorService.compareCompetitor).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareCollectionRun', () => {
      it('should forward to collectionRunService', () => {
        const entity = { id: 26304 };
        const entity2 = { id: 22231 };
        vitest.spyOn(collectionRunService, 'compareCollectionRun');
        comp.compareCollectionRun(entity, entity2);
        expect(collectionRunService.compareCollectionRun).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
