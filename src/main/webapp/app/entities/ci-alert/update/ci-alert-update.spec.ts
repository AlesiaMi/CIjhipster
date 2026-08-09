import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IAnalysisResult } from 'app/entities/analysis-result/analysis-result.model';
import { AnalysisResultService } from 'app/entities/analysis-result/service/analysis-result.service';
import { IAnalystProfile } from 'app/entities/analyst-profile/analyst-profile.model';
import { AnalystProfileService } from 'app/entities/analyst-profile/service/analyst-profile.service';
import { ICiAlert } from '../ci-alert.model';
import { CiAlertService } from '../service/ci-alert.service';

import { CiAlertFormService } from './ci-alert-form.service';
import { CiAlertUpdate } from './ci-alert-update';

describe('CiAlert Management Update Component', () => {
  let comp: CiAlertUpdate;
  let fixture: ComponentFixture<CiAlertUpdate>;
  let activatedRoute: ActivatedRoute;
  let ciAlertFormService: CiAlertFormService;
  let ciAlertService: CiAlertService;
  let analysisResultService: AnalysisResultService;
  let analystProfileService: AnalystProfileService;

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

    fixture = TestBed.createComponent(CiAlertUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    ciAlertFormService = TestBed.inject(CiAlertFormService);
    ciAlertService = TestBed.inject(CiAlertService);
    analysisResultService = TestBed.inject(AnalysisResultService);
    analystProfileService = TestBed.inject(AnalystProfileService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call AnalysisResult query and add missing value', () => {
      const ciAlert: ICiAlert = { id: 24603 };
      const analysisResult: IAnalysisResult = { id: 28488 };
      ciAlert.analysisResult = analysisResult;

      const analysisResultCollection: IAnalysisResult[] = [{ id: 28488 }];
      vitest.spyOn(analysisResultService, 'query').mockReturnValue(of(new HttpResponse({ body: analysisResultCollection })));
      const additionalAnalysisResults = [analysisResult];
      const expectedCollection: IAnalysisResult[] = [...additionalAnalysisResults, ...analysisResultCollection];
      vitest.spyOn(analysisResultService, 'addAnalysisResultToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ciAlert });
      comp.ngOnInit();

      expect(analysisResultService.query).toHaveBeenCalled();
      expect(analysisResultService.addAnalysisResultToCollectionIfMissing).toHaveBeenCalledWith(
        analysisResultCollection,
        ...additionalAnalysisResults.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.analysisResultsSharedCollection()).toEqual(expectedCollection);
    });

    it('should call AnalystProfile query and add missing value', () => {
      const ciAlert: ICiAlert = { id: 24603 };
      const analystProfile: IAnalystProfile = { id: 27303 };
      ciAlert.analystProfile = analystProfile;

      const analystProfileCollection: IAnalystProfile[] = [{ id: 27303 }];
      vitest.spyOn(analystProfileService, 'query').mockReturnValue(of(new HttpResponse({ body: analystProfileCollection })));
      const additionalAnalystProfiles = [analystProfile];
      const expectedCollection: IAnalystProfile[] = [...additionalAnalystProfiles, ...analystProfileCollection];
      vitest.spyOn(analystProfileService, 'addAnalystProfileToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ciAlert });
      comp.ngOnInit();

      expect(analystProfileService.query).toHaveBeenCalled();
      expect(analystProfileService.addAnalystProfileToCollectionIfMissing).toHaveBeenCalledWith(
        analystProfileCollection,
        ...additionalAnalystProfiles.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.analystProfilesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const ciAlert: ICiAlert = { id: 24603 };
      const analysisResult: IAnalysisResult = { id: 28488 };
      ciAlert.analysisResult = analysisResult;
      const analystProfile: IAnalystProfile = { id: 27303 };
      ciAlert.analystProfile = analystProfile;

      activatedRoute.data = of({ ciAlert });
      comp.ngOnInit();

      expect(comp.analysisResultsSharedCollection()).toContainEqual(analysisResult);
      expect(comp.analystProfilesSharedCollection()).toContainEqual(analystProfile);
      expect(comp.ciAlert).toEqual(ciAlert);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ICiAlert>();
      const ciAlert = { id: 1978 };
      vitest.spyOn(ciAlertFormService, 'getCiAlert').mockReturnValue(ciAlert);
      vitest.spyOn(ciAlertService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ciAlert });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(ciAlert);
      saveSubject.complete();

      // THEN
      expect(ciAlertFormService.getCiAlert).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(ciAlertService.update).toHaveBeenCalledWith(expect.objectContaining(ciAlert));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ICiAlert>();
      const ciAlert = { id: 1978 };
      vitest.spyOn(ciAlertFormService, 'getCiAlert').mockReturnValue({ id: null });
      vitest.spyOn(ciAlertService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ciAlert: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(ciAlert);
      saveSubject.complete();

      // THEN
      expect(ciAlertFormService.getCiAlert).toHaveBeenCalled();
      expect(ciAlertService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ICiAlert>();
      const ciAlert = { id: 1978 };
      vitest.spyOn(ciAlertService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ciAlert });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(ciAlertService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareAnalysisResult', () => {
      it('should forward to analysisResultService', () => {
        const entity = { id: 28488 };
        const entity2 = { id: 32267 };
        vitest.spyOn(analysisResultService, 'compareAnalysisResult');
        comp.compareAnalysisResult(entity, entity2);
        expect(analysisResultService.compareAnalysisResult).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareAnalystProfile', () => {
      it('should forward to analystProfileService', () => {
        const entity = { id: 27303 };
        const entity2 = { id: 15537 };
        vitest.spyOn(analystProfileService, 'compareAnalystProfile');
        comp.compareAnalystProfile(entity, entity2);
        expect(analystProfileService.compareAnalystProfile).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
