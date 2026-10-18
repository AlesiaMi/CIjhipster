import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ElementRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Navigation, Router, provideRouter } from '@angular/router';

import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { AccountService } from 'app/core/auth';

import Login from './login';
import { LoginService } from './login.service';

describe('Login', () => {
  let comp: Login;
  let fixture: ComponentFixture<Login>;
  let mockRouter: Router;
  let mockAccountService: AccountService;
  let mockLoginService: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideTranslateService(),
        {
          provide: ActivatedRoute,
          useValue: {},
        },
        {
          provide: AccountService,
          useValue: {
            isAuthenticated: vi.fn(() => false),
            identity: vi.fn(() => of(null)),
            hasAnyAuthority: vi.fn(() => false),
          },
        },
        {
          provide: LoginService,
          useValue: {
            login: vi.fn(() => of({})),
          },
        },
      ],
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Login);
    comp = fixture.componentInstance;

    mockRouter = TestBed.inject(Router);
    vi.spyOn(mockRouter, 'navigate').mockResolvedValue(true);

    mockLoginService = TestBed.inject(LoginService);
    mockAccountService = TestBed.inject(AccountService);
  });

  describe('ngOnInit', () => {
    it('should call accountService.identity on Init', () => {
      // GIVEN
      mockAccountService.identity = vi.fn(() => of(null));

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(mockAccountService.identity).toHaveBeenCalled();
    });

    it('should call accountService.isAuthenticated on Init', () => {
      // GIVEN
      mockAccountService.identity = vi.fn(() => of(null));

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(mockAccountService.isAuthenticated).toHaveBeenCalled();
    });

    it('should navigate to dashboard on Init if authenticated=true', () => {
      // GIVEN
      mockAccountService.identity = vi.fn(() => of(null));
      mockAccountService.isAuthenticated = vi.fn(() => true);
      vi.spyOn(mockAccountService, 'hasAnyAuthority').mockReturnValue(false);

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });
  });

  describe('ngAfterViewInit', () => {
    it('should set focus to username input after the view has been initialized', () => {
      // GIVEN
      const node = {
        focus: vi.fn(),
      };

      vi.spyOn(comp, 'username').mockReturnValue(new ElementRef(node));

      // WHEN
      comp.ngAfterViewInit();

      // THEN
      expect(node.focus).toHaveBeenCalled();
    });
  });

  describe('login', () => {
    it('should authenticate the user and navigate to dashboard', () => {
      // GIVEN
      const credentials = {
        username: 'user',
        password: 'user',
        rememberMe: true,
      };

      comp.loginForm.patchValue(credentials);

      vi.spyOn(mockRouter, 'currentNavigation').mockReturnValue(null);
      vi.spyOn(mockAccountService, 'hasAnyAuthority').mockReturnValue(false);

      // WHEN
      comp.login();

      // THEN
      expect(comp.authenticationError()).toEqual(false);
      expect(mockLoginService.login).toHaveBeenCalledWith(credentials);
      expect(mockAccountService.hasAnyAuthority).toHaveBeenCalledWith('ROLE_ADMIN');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });

    it('should navigate administrator to manager assignments after login', () => {
      // GIVEN
      const credentials = {
        username: 'admin',
        password: 'admin',
        rememberMe: true,
      };

      comp.loginForm.patchValue(credentials);

      vi.spyOn(mockRouter, 'currentNavigation').mockReturnValue(null);
      vi.spyOn(mockAccountService, 'hasAnyAuthority').mockReturnValue(true);

      // WHEN
      comp.login();

      // THEN
      expect(comp.authenticationError()).toEqual(false);
      expect(mockLoginService.login).toHaveBeenCalledWith(credentials);
      expect(mockAccountService.hasAnyAuthority).toHaveBeenCalledWith('ROLE_ADMIN');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/manager-assignments']);
    });

    it('should authenticate the user but not navigate if routing to a cached URL', () => {
      // GIVEN
      vi.spyOn(mockRouter, 'currentNavigation').mockReturnValue({} as Navigation);

      // WHEN
      comp.login();

      // THEN
      expect(comp.authenticationError()).toEqual(false);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should stay on login form and show error message on login error', () => {
      // GIVEN
      mockLoginService.login = vi.fn(() => throwError(() => new Error('Login failed')));

      // WHEN
      comp.login();

      // THEN
      expect(comp.authenticationError()).toEqual(true);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });
});
