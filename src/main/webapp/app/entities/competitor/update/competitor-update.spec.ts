import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IAnalystProfile } from 'app/entities/analyst-profile/analyst-profile.model';
import { AnalystProfileService } from 'app/entities/analyst-profile/service/analyst-profile.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { ICompetitor } from '../competitor.model';
import { CompetitorService } from '../service/competitor.service';

import { CompetitorFormService } from './competitor-form.service';
import { CompetitorUpdate } from './competitor-update';

describe('Competitor Management Update Component', () => {
  let comp: CompetitorUpdate;
  let fixture: ComponentFixture<CompetitorUpdate>;
  let activatedRoute: ActivatedRoute;
  let competitorFormService: CompetitorFormService;
  let competitorService: CompetitorService;
  let userService: UserService;
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

    fixture = TestBed.createComponent(CompetitorUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    competitorFormService = TestBed.inject(CompetitorFormService);
    competitorService = TestBed.inject(CompetitorService);
    userService = TestBed.inject(UserService);
    analystProfileService = TestBed.inject(AnalystProfileService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const competitor: ICompetitor = { id: 15804 };
      const owner: IUser = { id: 3944 };
      competitor.owner = owner;

      const userCollection: IUser[] = [{ id: 3944 }];
      vitest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [owner];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vitest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ competitor });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call AnalystProfile query and add missing value', () => {
      const competitor: ICompetitor = { id: 15804 };
      const analystProfileses: IAnalystProfile[] = [{ id: 27303 }];
      competitor.analystProfileses = analystProfileses;

      const analystProfileCollection: IAnalystProfile[] = [{ id: 27303 }];
      vitest.spyOn(analystProfileService, 'query').mockReturnValue(of(new HttpResponse({ body: analystProfileCollection })));
      const additionalAnalystProfiles = [...analystProfileses];
      const expectedCollection: IAnalystProfile[] = [...additionalAnalystProfiles, ...analystProfileCollection];
      vitest.spyOn(analystProfileService, 'addAnalystProfileToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ competitor });
      comp.ngOnInit();

      expect(analystProfileService.query).toHaveBeenCalled();
      expect(analystProfileService.addAnalystProfileToCollectionIfMissing).toHaveBeenCalledWith(
        analystProfileCollection,
        ...additionalAnalystProfiles.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.analystProfilesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const competitor: ICompetitor = { id: 15804 };
      const owner: IUser = { id: 3944 };
      competitor.owner = owner;
      const analystProfiles: IAnalystProfile = { id: 27303 };
      competitor.analystProfileses = [analystProfiles];

      activatedRoute.data = of({ competitor });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(owner);
      expect(comp.analystProfilesSharedCollection()).toContainEqual(analystProfiles);
      expect(comp.competitor).toEqual(competitor);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ICompetitor>();
      const competitor = { id: 6672 };
      vitest.spyOn(competitorFormService, 'getCompetitor').mockReturnValue(competitor);
      vitest.spyOn(competitorService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ competitor });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(competitor);
      saveSubject.complete();

      // THEN
      expect(competitorFormService.getCompetitor).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(competitorService.update).toHaveBeenCalledWith(expect.objectContaining(competitor));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ICompetitor>();
      const competitor = { id: 6672 };
      vitest.spyOn(competitorFormService, 'getCompetitor').mockReturnValue({ id: null });
      vitest.spyOn(competitorService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ competitor: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(competitor);
      saveSubject.complete();

      // THEN
      expect(competitorFormService.getCompetitor).toHaveBeenCalled();
      expect(competitorService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ICompetitor>();
      const competitor = { id: 6672 };
      vitest.spyOn(competitorService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ competitor });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(competitorService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        vitest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
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
