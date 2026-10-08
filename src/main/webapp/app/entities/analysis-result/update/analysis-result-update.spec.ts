import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { INewsItem } from 'app/entities/news-item/news-item.model';
import { NewsItemService } from 'app/entities/news-item/service/news-item.service';
import { IAnalysisResult } from '../analysis-result.model';
import { AnalysisResultService } from '../service/analysis-result.service';

import { AnalysisResultFormService } from './analysis-result-form.service';
import { AnalysisResultUpdate } from './analysis-result-update';

describe('AnalysisResult Management Update Component', () => {
  let comp: AnalysisResultUpdate;
  let fixture: ComponentFixture<AnalysisResultUpdate>;
  let activatedRoute: ActivatedRoute;
  let analysisResultFormService: AnalysisResultFormService;
  let analysisResultService: AnalysisResultService;
  let newsItemService: NewsItemService;

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

    fixture = TestBed.createComponent(AnalysisResultUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    analysisResultFormService = TestBed.inject(AnalysisResultFormService);
    analysisResultService = TestBed.inject(AnalysisResultService);
    newsItemService = TestBed.inject(NewsItemService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call newsItem query and add missing value', () => {
      const analysisResult: IAnalysisResult = { id: 32267 };
      const newsItem: INewsItem = { id: 25999 };
      analysisResult.newsItem = newsItem;

      const newsItemCollection: INewsItem[] = [{ id: 25999 }];
      vitest.spyOn(newsItemService, 'query').mockReturnValue(of(new HttpResponse({ body: newsItemCollection })));
      const expectedCollection: INewsItem[] = [newsItem, ...newsItemCollection];
      vitest.spyOn(newsItemService, 'addNewsItemToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ analysisResult });
      comp.ngOnInit();

      expect(newsItemService.query).toHaveBeenCalled();
      expect(newsItemService.addNewsItemToCollectionIfMissing).toHaveBeenCalledWith(newsItemCollection, newsItem);
      expect(comp.newsItemsCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const analysisResult: IAnalysisResult = { id: 32267 };
      const newsItem: INewsItem = { id: 25999 };
      analysisResult.newsItem = newsItem;

      activatedRoute.data = of({ analysisResult });
      comp.ngOnInit();

      expect(comp.newsItemsCollection()).toContainEqual(newsItem);
      expect(comp.analysisResult).toEqual(analysisResult);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAnalysisResult>();
      const analysisResult = { id: 28488 };
      vitest.spyOn(analysisResultFormService, 'getAnalysisResult').mockReturnValue(analysisResult);
      vitest.spyOn(analysisResultService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ analysisResult });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(analysisResult);
      saveSubject.complete();

      // THEN
      expect(analysisResultFormService.getAnalysisResult).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(analysisResultService.update).toHaveBeenCalledWith(expect.objectContaining(analysisResult));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAnalysisResult>();
      const analysisResult = { id: 28488 };
      vitest.spyOn(analysisResultFormService, 'getAnalysisResult').mockReturnValue({ id: null });
      vitest.spyOn(analysisResultService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ analysisResult: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(analysisResult);
      saveSubject.complete();

      // THEN
      expect(analysisResultFormService.getAnalysisResult).toHaveBeenCalled();
      expect(analysisResultService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IAnalysisResult>();
      const analysisResult = { id: 28488 };
      vitest.spyOn(analysisResultService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ analysisResult });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(analysisResultService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareNewsItem', () => {
      it('should forward to newsItemService', () => {
        const entity = { id: 25999 };
        const entity2 = { id: 14522 };
        vitest.spyOn(newsItemService, 'compareNewsItem');
        comp.compareNewsItem(entity, entity2);
        expect(newsItemService.compareNewsItem).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
