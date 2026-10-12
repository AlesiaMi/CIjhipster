import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AccountService } from 'app/core/auth';
import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
  imports: [TranslateDirective, RouterLink],
})
export default class Home implements OnInit {
  readonly account = inject(AccountService).account;
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      if (!account) {
        return;
      }

      if (account.authorities.includes('ROLE_ADMIN')) {
        void this.router.navigate(['/admin/manager-assignments']);
        return;
      }

      if (account.authorities.includes('ROLE_MANAGER') || account.authorities.includes('ROLE_USER')) {
        void this.router.navigate(['/dashboard']);
      }
    });
  }

  login(): void {
    void this.router.navigate(['/login']);
  }
}
