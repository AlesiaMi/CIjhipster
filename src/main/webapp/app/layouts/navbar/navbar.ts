import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbCollapse } from '@ng-bootstrap/ng-bootstrap/collapse';
import { NgbDropdown, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap/dropdown';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { environment } from 'environments/environment';

import { LANGUAGES } from 'app/config';
import { AccountService, StateStorageService } from 'app/core/auth';
import { ManagerClientContextService } from 'app/core/manager/manager-client-context.service';
import { ProfileService } from 'app/layouts/profiles/profile.service';
import { LoginService } from 'app/login/login.service';
import { HasAnyAuthorityDirective } from 'app/shared/auth';
import { FindLanguageFromKeyPipe, TranslateDirective } from 'app/shared/language';

import ActiveMenuDirective from './active-menu.directive';

@Component({
  selector: 'jhi-navbar',
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
  imports: [
    RouterLink,
    RouterLinkActive,
    FontAwesomeModule,
    NgbCollapse,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    HasAnyAuthorityDirective,
    ActiveMenuDirective,
    FindLanguageFromKeyPipe,
    TranslateDirective,
    TranslateModule,
  ],
})
export default class Navbar implements OnInit {
  readonly inProduction = signal(true);
  readonly isNavbarCollapsed = signal(true);
  readonly languages = LANGUAGES;
  readonly openAPIEnabled = signal(false);
  readonly version: string;

  readonly account = inject(AccountService).account;

  // Контекст менеджера и выбранного клиента.
  readonly managerClientContext = inject(ManagerClientContextService);

  private readonly loginService = inject(LoginService);
  private readonly translateService = inject(TranslateService);
  private readonly stateStorageService = inject(StateStorageService);
  private readonly profileService = inject(ProfileService);
  private readonly router = inject(Router);

  constructor() {
    const { VERSION } = environment;

    if (VERSION) {
      this.version = VERSION.toLowerCase().startsWith('v') ? VERSION : `v${VERSION}`;
    } else {
      this.version = '';
    }
  }

  ngOnInit(): void {
    // Получаем настройки приложения.
    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction.set(profileInfo.inProduction ?? true);
      this.openAPIEnabled.set(profileInfo.openAPIEnabled ?? false);
    });

    // Инициализируем контекст менеджера.
    this.managerClientContext.initialize().subscribe();
  }

  changeLanguage(languageKey: string): void {
    this.stateStorageService.storeLocale(languageKey);
    this.translateService.use(languageKey);
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed.set(true);
  }

  login(): void {
    void this.router.navigate(['/login']);
  }

  logout(): void {
    this.collapseNavbar();
    this.loginService.logout();
    void this.router.navigate(['']);
  }
}
