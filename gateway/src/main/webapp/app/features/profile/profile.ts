import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { AccountService } from 'app/core/auth/account.service';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';

@Component({
  selector: 'jhi-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
  imports: [RouterLink, FontAwesomeModule, PageHeader, SectionPanel, StatePanel],
})
export default class Profile {
  readonly account = inject(AccountService).account;

  readonly displayName = computed(() => {
    const account = this.account();
    if (!account) {
      return 'Profile';
    }
    return [account.firstName, account.lastName].filter(Boolean).join(' ').trim() || account.login;
  });

  readonly initial = computed(() => this.displayName().charAt(0).toUpperCase());
}
