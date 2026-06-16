import {
  ChangeDetectionStrategy,
  Component,
  DOCUMENT,
  DestroyRef,
  OnInit,
  Renderer2,
  RendererFactory2,
  inject,
  signal,
} from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';
import { filter } from 'rxjs';

import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import { AccountService } from 'app/core/auth/account.service';
import Footer from '../footer/footer';

@Component({
  selector: 'jhi-main',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './main.html',
  providers: [AppPageTitleStrategy],
  imports: [RouterOutlet, Footer],
})
export default class Main implements OnInit {
  readonly isAppShellRoute = signal(false);
  readonly isFullViewportRoute = signal(false);

  private readonly renderer: Renderer2;
  private readonly htmlElement: HTMLElement;

  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly appPageTitleStrategy = inject(AppPageTitleStrategy);
  private readonly accountService = inject(AccountService);
  private readonly document = inject(DOCUMENT);
  private readonly translateService = inject(TranslateService);
  private readonly rootRenderer = inject(RendererFactory2);

  constructor() {
    this.htmlElement = this.document.documentElement;
    this.renderer = this.rootRenderer.createRenderer(this.htmlElement, null);
  }

  ngOnInit(): void {
    this.syncShellRoute(this.router.url);
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(event => this.syncShellRoute(event.urlAfterRedirects));

    // try to log in automatically
    this.accountService.identity().subscribe();

    this.translateService.onLangChange.subscribe((langChangeEvent: LangChangeEvent) => {
      this.appPageTitleStrategy.updateTitle(this.router.routerState.snapshot);
      dayjs.locale(langChangeEvent.lang);
      this.renderer.setAttribute(this.htmlElement, 'lang', langChangeEvent.lang);
    });
  }

  private syncShellRoute(url: string): void {
    this.isAppShellRoute.set(
      url === '/app' ||
        url.startsWith('/app/') ||
        url === '/projects' ||
        url.startsWith('/projects/') ||
        url === '/notifications' ||
        url.startsWith('/notifications/'),
    );
    this.isFullViewportRoute.set(
      this.isAppShellRoute() ||
        url === '/' ||
        url === '/login' ||
        url.startsWith('/account/register') ||
        url.startsWith('/account/settings') ||
        url.startsWith('/account/password') ||
        url.startsWith('/account/reset/'),
    );
  }
}
