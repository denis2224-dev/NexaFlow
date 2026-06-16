import { Injectable, effect, inject, signal, untracked } from '@angular/core';

import { EMPTY, Observable, Subscription, catchError, finalize, tap, throwError, timer } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import { INotification } from './notification.model';
import { NotificationService } from './notification.service';

@Injectable({ providedIn: 'root' })
export class NotificationStateService {
  readonly unreadCount = signal(0);
  readonly latestNotifications = signal<INotification[]>([]);
  readonly loading = signal(false);
  readonly actionInProgress = signal(false);

  private readonly accountService = inject(AccountService);
  private readonly activeOrganizationService = inject(ActiveOrganizationService);
  private readonly notificationService = inject(NotificationService);
  private pollingSubscription: Subscription | null = null;
  private unreadRequestInFlight = false;
  private latestRequestInFlight = false;
  private lastOrganizationId: number | null | undefined = undefined;

  constructor() {
    effect(() => {
      const account = this.accountService.account();
      const organizationId = this.activeOrganizationService.selectedOrganizationId();
      const organizationLoading = this.activeOrganizationService.isLoading();

      if (!account) {
        untracked(() => this.clearState());
        return;
      }

      if (organizationLoading) {
        return;
      }

      if (this.lastOrganizationId !== organizationId) {
        this.lastOrganizationId = organizationId;
        untracked(() => this.initializeNotifications());
      }
    });
  }

  initializeNotifications(): void {
    if (!this.accountService.isAuthenticated()) {
      this.clearState();
      return;
    }

    this.refreshUnreadCount();
    this.refreshLatestNotifications();
    this.startPolling();
  }

  refreshUnreadCount(): void {
    if (!this.accountService.isAuthenticated() || this.unreadRequestInFlight) {
      return;
    }

    this.unreadRequestInFlight = true;
    this.notificationService
      .getUnreadCount()
      .pipe(
        catchError(() => EMPTY),
        finalize(() => {
          this.unreadRequestInFlight = false;
        }),
      )
      .subscribe(count => this.unreadCount.set(Math.max(0, count)));
  }

  refreshLatestNotifications(): void {
    if (!this.accountService.isAuthenticated() || this.latestRequestInFlight) {
      return;
    }

    this.latestRequestInFlight = true;
    this.loading.set(true);
    this.notificationService
      .getLatestNotifications(5)
      .pipe(
        catchError(() => EMPTY),
        finalize(() => {
          this.latestRequestInFlight = false;
          this.loading.set(false);
        }),
      )
      .subscribe(notifications => this.latestNotifications.set(notifications));
  }

  markAsRead(notification: INotification): Observable<INotification> {
    if (notification.isRead || this.actionInProgress()) {
      return EMPTY;
    }

    this.actionInProgress.set(true);
    const previousUnreadCount = this.unreadCount();
    this.updateNotification(notification.id, { isRead: true });
    this.unreadCount.set(Math.max(0, previousUnreadCount - 1));

    return this.notificationService.markAsRead(notification.id).pipe(
      tap(updatedNotification => this.updateNotification(notification.id, updatedNotification)),
      catchError(error => {
        this.updateNotification(notification.id, { isRead: false });
        this.unreadCount.set(previousUnreadCount);
        return throwError(() => error);
      }),
      finalize(() => this.actionInProgress.set(false)),
    );
  }

  markAllAsRead(): Observable<void> {
    if (this.actionInProgress()) {
      return EMPTY;
    }

    this.actionInProgress.set(true);
    const previousUnreadCount = this.unreadCount();
    const previousLatestNotifications = this.latestNotifications();
    this.unreadCount.set(0);
    this.latestNotifications.update(notifications => notifications.map(notification => ({ ...notification, isRead: true })));

    return this.notificationService.markAllAsRead().pipe(
      catchError(error => {
        this.unreadCount.set(previousUnreadCount);
        this.latestNotifications.set(previousLatestNotifications);
        return throwError(() => error);
      }),
      finalize(() => this.actionInProgress.set(false)),
    );
  }

  clearState(): void {
    this.stopPolling();
    this.lastOrganizationId = undefined;
    this.unreadCount.set(0);
    this.latestNotifications.set([]);
    this.loading.set(false);
    this.actionInProgress.set(false);
  }

  private updateNotification(id: number, patch: Partial<INotification>): void {
    this.latestNotifications.update(notifications =>
      notifications.map(notification => (notification.id === id ? { ...notification, ...patch } : notification)),
    );
  }

  private startPolling(): void {
    if (this.pollingSubscription) {
      return;
    }

    this.pollingSubscription = timer(30000, 30000).subscribe(() => this.refreshUnreadCount());
  }

  private stopPolling(): void {
    this.pollingSubscription?.unsubscribe();
    this.pollingSubscription = null;
  }
}
