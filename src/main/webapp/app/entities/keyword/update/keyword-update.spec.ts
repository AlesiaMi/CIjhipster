import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { IKeyword } from '../keyword.model';
import { KeywordService } from '../service/keyword.service';

import { KeywordFormService } from './keyword-form.service';
import { KeywordUpdate } from './keyword-update';

describe('Keyword Management Update Component', () => {
  let comp: KeywordUpdate;
  let fixture: ComponentFixture<KeywordUpdate>;
  let activatedRoute: ActivatedRoute;
  let keywordFormService: KeywordFormService;
  let keywordService: KeywordService;
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

    fixture = TestBed.createComponent(KeywordUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    keywordFormService = TestBed.inject(KeywordFormService);
    keywordService = TestBed.inject(KeywordService);
    competitorService = TestBed.inject(CompetitorService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Competitor query and add missing value', () => {
      const keyword: IKeyword = { id: 31567 };
      const competitor: ICompetitor = { id: 6672 };
      keyword.competitor = competitor;

      const competitorCollection: ICompetitor[] = [{ id: 6672 }];
      vitest.spyOn(competitorService, 'query').mockReturnValue(of(new HttpResponse({ body: competitorCollection })));
      const additionalCompetitors = [competitor];
      const expectedCollection: ICompetitor[] = [...additionalCompetitors, ...competitorCollection];
      vitest.spyOn(competitorService, 'addCompetitorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ keyword });
      comp.ngOnInit();

      expect(competitorService.query).toHaveBeenCalled();
      expect(competitorService.addCompetitorToCollectionIfMissing).toHaveBeenCalledWith(
        competitorCollection,
        ...additionalCompetitors.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.competitorsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const keyword: IKeyword = { id: 31567 };
      const competitor: ICompetitor = { id: 6672 };
      keyword.competitor = competitor;

      activatedRoute.data = of({ keyword });
      comp.ngOnInit();

      expect(comp.competitorsSharedCollection()).toContainEqual(competitor);
      expect(comp.keyword).toEqual(keyword);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IKeyword>();
      const keyword = { id: 5188 };
      vitest.spyOn(keywordFormService, 'getKeyword').mockReturnValue(keyword);
      vitest.spyOn(keywordService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ keyword });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(keyword);
      saveSubject.complete();

      // THEN
      expect(keywordFormService.getKeyword).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(keywordService.update).toHaveBeenCalledWith(expect.objectContaining(keyword));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IKeyword>();
      const keyword = { id: 5188 };
      vitest.spyOn(keywordFormService, 'getKeyword').mockReturnValue({ id: null });
      vitest.spyOn(keywordService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ keyword: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(keyword);
      saveSubject.complete();

      // THEN
      expect(keywordFormService.getKeyword).toHaveBeenCalled();
      expect(keywordService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IKeyword>();
      const keyword = { id: 5188 };
      vitest.spyOn(keywordService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ keyword });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(keywordService.update).toHaveBeenCalled();
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
