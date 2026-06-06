import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { filter } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { Authority } from 'app/shared/jhipster/constants';

type ShellNavItem = {
  label: string;
  route: string;
  icon: string;
  exact?: boolean;
};

@Component({
  selector: 'jhi-app-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.scss',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FontAwesomeModule],
})
export default class AppShell {
  readonly sidebarCollapsed = signal(false);
  readonly mobileSidebarOpen = signal(false);
  readonly currentUrl = signal('');
  readonly account = inject(AccountService).account;

  readonly navItems: ShellNavItem[] = [
    { label: 'Dashboard', route: '/app/dashboard', icon: 'tachometer-alt', exact: true },
    { label: 'Organizations', route: '/app/organizations', icon: 'users' },
    { label: 'Profile', route: '/app/profile', icon: 'user', exact: true },
  ];

  readonly pageTitle = computed(() => {
    const url = this.currentUrl();
    if (url.startsWith('/app/organizations')) {
      return 'Organizations';
    }
    if (url.startsWith('/app/profile')) {
      return 'Profile';
    }
    return 'Dashboard';
  });

  readonly displayName = computed(() => {
    const account = this.account();
    if (!account) {
      return 'Account';
    }
    const fullName = [account.firstName, account.lastName].filter(Boolean).join(' ').trim();
    return fullName || account.login;
  });

  readonly userInitial = computed(() => this.displayName().charAt(0).toUpperCase());

  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly accountService = inject(AccountService);
  private readonly loginService = inject(LoginService);

  constructor() {
    this.currentUrl.set(this.router.url);
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(event => {
        this.currentUrl.set(event.urlAfterRedirects);
        this.mobileSidebarOpen.set(false);
      });
  }

  isAdmin(): boolean {
    return this.accountService.hasAnyAuthority(Authority.ADMIN);
  }

  toggleSidebar(): void {
    this.sidebarCollapsed.update(value => !value);
  }

  toggleMobileSidebar(): void {
    this.mobileSidebarOpen.update(value => !value);
  }

  logout(): void {
    this.loginService.logout();
    this.router.navigate(['/login']);
  }
}
