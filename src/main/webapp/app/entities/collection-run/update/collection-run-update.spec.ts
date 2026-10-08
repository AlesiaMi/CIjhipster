import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { ICollectionRun } from '../collection-run.model';
import { CollectionRunService } from '../service/collection-run.service';

import { CollectionRunFormService } from './collection-run-form.service';
import { CollectionRunUpdate } from './collection-run-update';

describe('CollectionRun Management Update Component', () => {
  let comp: CollectionRunUpdate;
  let fixture: ComponentFixture<CollectionRunUpdate>;
  let activatedRoute: ActivatedRoute;
  let collectionRunFormService: CollectionRunFormService;
  let collectionRunService: CollectionRunService;
  let userService: UserService;

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

    fixture = TestBed.createComponent(CollectionRunUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    collectionRunFormService = TestBed.inject(CollectionRunFormService);
    collectionRunService = TestBed.inject(CollectionRunService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const collectionRun: ICollectionRun = { id: 22231 };
      const owner: IUser = { id: 3944 };
      collectionRun.owner = owner;

      const userCollection: IUser[] = [{ id: 3944 }];
      vitest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [owner];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vitest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ collectionRun });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const collectionRun: ICollectionRun = { id: 22231 };
      const owner: IUser = { id: 3944 };
      collectionRun.owner = owner;

      activatedRoute.data = of({ collectionRun });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(owner);
      expect(comp.collectionRun).toEqual(collectionRun);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ICollectionRun>();
      const collectionRun = { id: 26304 };
      vitest.spyOn(collectionRunFormService, 'getCollectionRun').mockReturnValue(collectionRun);
      vitest.spyOn(collectionRunService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ collectionRun });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(collectionRun);
      saveSubject.complete();

      // THEN
      expect(collectionRunFormService.getCollectionRun).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(collectionRunService.update).toHaveBeenCalledWith(expect.objectContaining(collectionRun));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ICollectionRun>();
      const collectionRun = { id: 26304 };
      vitest.spyOn(collectionRunFormService, 'getCollectionRun').mockReturnValue({ id: null });
      vitest.spyOn(collectionRunService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ collectionRun: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(collectionRun);
      saveSubject.complete();

      // THEN
      expect(collectionRunFormService.getCollectionRun).toHaveBeenCalled();
      expect(collectionRunService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ICollectionRun>();
      const collectionRun = { id: 26304 };
      vitest.spyOn(collectionRunService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ collectionRun });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(collectionRunService.update).toHaveBeenCalled();
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
  });
});
