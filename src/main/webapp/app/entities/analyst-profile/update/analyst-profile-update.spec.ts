import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ICompetitor } from 'app/entities/competitor/competitor.model';
import { CompetitorService } from 'app/entities/competitor/service/competitor.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { IAnalystProfile } from '../analyst-profile.model';
import { AnalystProfileService } from '../service/analyst-profile.service';

import { AnalystProfileFormService } from './analyst-profile-form.service';
import { AnalystProfileUpdate } from './analyst-profile-update';

describe('AnalystProfile Management Update Component', () => {
  let comp: AnalystProfileUpdate;
  let fixture: ComponentFixture<AnalystProfileUpdate>;
  let activatedRoute: ActivatedRoute;
  let analystProfileFormService: AnalystProfileFormService;
  let analystProfileService: AnalystProfileService;
  let userService: UserService;
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

    fixture = TestBed.createComponent(AnalystProfileUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    analystProfileFormService = TestBed.inject(AnalystProfileFormService);
    analystProfileService = TestBed.inject(AnalystProfileService);
    userService = TestBed.inject(UserService);
    competitorService = TestBed.inject(CompetitorService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const analystProfile: IAnalystProfile = { id: 15537 };
      const user: IUser = { id: 3944 };
      analystProfile.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      vitest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vitest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ analystProfile });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Competitor query and add missing value', () => {
      const analystProfile: IAnalystProfile = { id: 15537 };
      const competitorses: ICompetitor[] = [{ id: 6672 }];
      analystProfile.competitorses = competitorses;

      const competitorCollection: ICompetitor[] = [{ id: 6672 }];
      vitest.spyOn(competitorService, 'query').mockReturnValue(of(new HttpResponse({ body: competitorCollection })));
      const additionalCompetitors = [...competitorses];
      const expectedCollection: ICompetitor[] = [...additionalCompetitors, ...competitorCollection];
      vitest.spyOn(competitorService, 'addCompetitorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ analystProfile });
      comp.ngOnInit();

      expect(competitorService.query).toHaveBeenCalled();
      expect(competitorService.addCompetitorToCollectionIfMissing).toHaveBeenCalledWith(
        competitorCollection,
        ...additionalCompetitors.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.competitorsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const analystProfile: IAnalystProfile = { id: 15537 };
      const user: IUser = { id: 3944 };
      analystProfile.user = user;
      const competitors: ICompetitor = { id: 6672 };
      analystProfile.competitorses = [competitors];

      activatedRoute.data = of({ analystProfile });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(user);
      expect(comp.competitorsSharedCollection()).toContainEqual(competitors);
      expect(comp.analystProfile).toEqual(analystProfile);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAnalystProfile>();
      const analystProfile = { id: 27303 };
      vitest.spyOn(analystProfileFormService, 'getAnalystProfile').mockReturnValue(analystProfile);
      vitest.spyOn(analystProfileService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ analystProfile });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(analystProfile);
      saveSubject.complete();

      // THEN
      expect(analystProfileFormService.getAnalystProfile).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(analystProfileService.update).toHaveBeenCalledWith(expect.objectContaining(analystProfile));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAnalystProfile>();
      const analystProfile = { id: 27303 };
      vitest.spyOn(analystProfileFormService, 'getAnalystProfile').mockReturnValue({ id: null });
      vitest.spyOn(analystProfileService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ analystProfile: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(analystProfile);
      saveSubject.complete();

      // THEN
      expect(analystProfileFormService.getAnalystProfile).toHaveBeenCalled();
      expect(analystProfileService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IAnalystProfile>();
      const analystProfile = { id: 27303 };
      vitest.spyOn(analystProfileService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ analystProfile });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(analystProfileService.update).toHaveBeenCalled();
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
