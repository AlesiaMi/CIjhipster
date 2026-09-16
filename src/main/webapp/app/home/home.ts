import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './home.html',
  styleUrl: './home.scss',
  imports: [TranslateDirective, TranslateModule, RouterLink],
})
export default class Home implements OnInit {
  private readonly accountService = inject(AccountService);
  public readonly account = this.accountService.account;
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      if (!account) {
        return;
      }
      if (account.authorities.includes('ROLE_ADMIN')) {
        this.router.navigate(['/admin/manager-assignments']);
        return;
      }
      if (account.authorities.includes('ROLE_MANAGER') || account.authorities.includes('ROLE_USER')) {
        this.router.navigate(['/dashboard']);
      }
    });
  }
  login(): void {
    this.router.navigate(['/login']);
  }
}
