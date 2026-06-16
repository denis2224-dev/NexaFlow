import { DatePipe, NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { PaginatorModule } from 'primeng/paginator';
import { SelectModule } from 'primeng/select';
import { SkeletonModule } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { finalize } from 'rxjs';

import { ActiveOrganizationService } from 'app/core/nexaflow/active-organization.service';
import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import {
  INotification,
  NotificationReadStatusFilter,
  NotificationType,
} from 'app/shared/notification/notification.model';
import { NotificationNavigationService } from 'app/shared/notification/notification-navigation.service';
import { NotificationService } from 'app/shared/notification/notification.service';
import { NotificationStateService } from 'app/shared/notification/notification-state.service';
import PageHeader from 'app/shared/ui/page-header/page-header';
import SectionPanel from 'app/shared/ui/section-panel/section-panel';
import StatePanel from 'app/shared/ui/state-panel/state-panel';

type TagSeverity = 'success' | 'info' | 'secondary' | 'warn' | 'danger' | 'contrast';
type NotificationTypeFilter = NotificationType | 'ALL';
type PageEvent = { first?: number; rows?: number };
type SelectOption<T> = { label: string; value: T };

@Component({
  selector: 'jhi-notifications',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.scss',
  imports: [
    DatePipe,
    NgClass,
    FormsModule,
    ButtonModule,
    PaginatorModule,
    SelectModule,
    SkeletonModule,
    TagModule,
    ToastModule,
    PageHeader,
    SectionPanel,
    StatePanel,
  ],
})
export default class NotificationsComponent implements OnInit {
  readonly notifications = signal<INotification[]>([]);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly workspaceName = signal<string | null>(null);
  readonly totalItems = signal(0);
  readonly hasKnownTotal = signal(false);
  readonly currentPageItemCount = signal(0);
  readonly rows = signal(20);
  readonly first = signal(0);
  readonly readStatusFilter = signal<NotificationReadStatusFilter>('ALL');
  readonly typeFilter = signal<NotificationTypeFilter>('ALL');

  readonly readStatusOptions: SelectOption<NotificationReadStatusFilter>[] = [
    { label: 'All', value: 'ALL' },
    { label: 'Unread', value: 'UNREAD' },
    { label: 'Read', value: 'READ' },
  ];

  readonly typeOptions: SelectOption<NotificationTypeFilter>[] = [
    { label: 'All', value: 'ALL' },
    { label: 'Task assigned', value: 'TASK_ASSIGNED' },
    { label: 'Comment added', value: 'COMMENT_ADDED' },
    { label: 'Project updated', value: 'PROJECT_UPDATED' },
    { label: 'Invitation accepted', value: 'INVITATION_ACCEPTED' },
    { label: 'Subscription changed', value: 'SUBSCRIPTION_CHANGED' },
    { label: 'System', value: 'SYSTEM' },
  ];

  readonly filteredNotifications = computed(() => {
    const readFilter = this.readStatusFilter();
    const typeFilter = this.typeFilter();

    return this.notifications().filter(notification => {
      const readMatches =
        readFilter === 'ALL' || (readFilter === 'READ' && notification.isRead) || (readFilter === 'UNREAD' && !notification.isRead);
      const typeMatches = typeFilter === 'ALL' || notification.type === typeFilter;
      return readMatches && typeMatches;
    });
  });

  readonly pageNumber = computed(() => Math.floor(this.first() / this.rows()) + 1);
  readonly canGoNext = computed(() => !this.hasKnownTotal() && this.currentPageItemCount() === this.rows());

  private readonly activeOrganizationService = inject(ActiveOrganizationService);
  private readonly notificationService = inject(NotificationService);
  readonly notificationStateService = inject(NotificationStateService);
  private readonly notificationNavigationService = inject(NotificationNavigationService);
  private readonly messageService = inject(MessageService);
  private lastOrganizationId: number | null | undefined = undefined;

  constructor() {
    effect(() => {
      const activeOrganization = this.activeOrganizationService.activeOrganization();
      const isOrganizationLoading = this.activeOrganizationService.isLoading();

      if (isOrganizationLoading) {
        this.isLoading.set(true);
        return;
      }

      const organizationId = activeOrganization?.organizationId ?? null;
      this.workspaceName.set(activeOrganization?.workspace.name ?? activeOrganization?.workspace.slug ?? null);

      if (this.lastOrganizationId !== organizationId) {
        this.lastOrganizationId = organizationId;
        this.first.set(0);
        untracked(() => this.loadNotifications());
      }
    });
  }

  ngOnInit(): void {
    this.activeOrganizationService.loadOrganizations();
    this.notificationStateService.initializeNotifications();
  }

  loadNotifications(event?: PageEvent): void {
    const rows = event?.rows ?? this.rows();
    const first = event?.first ?? this.first();
    const page = Math.floor(first / rows);

    this.rows.set(rows);
    this.first.set(first);
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.notificationService
      .getMyNotifications(page, rows, 'createdAt,desc')
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: response => {
          const body = response.body ?? [];
          const totalCountHeader = response.headers.get('X-Total-Count');

          this.notifications.set(body);
          this.currentPageItemCount.set(body.length);
          this.hasKnownTotal.set(totalCountHeader !== null);
          this.totalItems.set(totalCountHeader !== null ? Number(totalCountHeader) : body.length);
        },
        error: error => {
          this.notifications.set([]);
          this.currentPageItemCount.set(0);
          this.errorMessage.set(extractNexaFlowErrorMessage(error, 'Notifications could not be loaded.'));
        },
      });
  }

  refresh(): void {
    this.loadNotifications();
    this.notificationStateService.refreshUnreadCount();
    this.notificationStateService.refreshLatestNotifications();
  }

  setReadStatusFilter(value: NotificationReadStatusFilter): void {
    this.readStatusFilter.set(value);
  }

  setTypeFilter(value: NotificationTypeFilter): void {
    this.typeFilter.set(value);
  }

  previousPage(): void {
    if (this.first() === 0) {
      return;
    }

    this.loadNotifications({ first: Math.max(0, this.first() - this.rows()), rows: this.rows() });
  }

  nextPage(): void {
    if (!this.canGoNext()) {
      return;
    }

    this.loadNotifications({ first: this.first() + this.rows(), rows: this.rows() });
  }

  markAsRead(notification: INotification, event?: Event): void {
    event?.stopPropagation();
    if (notification.isRead) {
      return;
    }

    this.notificationStateService.markAsRead(notification).subscribe({
      next: updatedNotification => this.patchNotification(notification.id, updatedNotification),
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Notification not updated',
          detail: extractNexaFlowErrorMessage(error, 'Notification could not be marked as read.'),
        });
      },
    });
  }

  markAllAsRead(): void {
    if (this.notificationStateService.unreadCount() === 0) {
      return;
    }

    this.notificationStateService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update(notifications => notifications.map(notification => ({ ...notification, isRead: true })));
        this.messageService.add({ severity: 'success', summary: 'Notifications updated', detail: 'All notifications marked as read.' });
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Notifications not updated',
          detail: extractNexaFlowErrorMessage(error, 'Notifications could not be marked as read.'),
        });
      },
    });
  }

  openNotification(notification: INotification): void {
    const navigate = (): void => {
      void this.notificationNavigationService.navigate(notification);
    };

    if (notification.isRead) {
      navigate();
      return;
    }

    this.notificationStateService.markAsRead(notification).subscribe({
      next: updatedNotification => {
        this.patchNotification(notification.id, updatedNotification);
        navigate();
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Notification not updated',
          detail: extractNexaFlowErrorMessage(error, 'Notification could not be marked as read.'),
        });
      },
    });
  }

  labelFor(type: NotificationType): string {
    return this.typeOptions.find(option => option.value === type)?.label ?? 'Notification';
  }

  iconFor(type: NotificationType): string {
    const icons: Record<NotificationType, string> = {
      TASK_ASSIGNED: 'pi pi-check-square',
      COMMENT_ADDED: 'pi pi-comments',
      PROJECT_UPDATED: 'pi pi-folder',
      INVITATION_ACCEPTED: 'pi pi-user-plus',
      SUBSCRIPTION_CHANGED: 'pi pi-credit-card',
      SYSTEM: 'pi pi-info-circle',
    };

    return icons[type] ?? 'pi pi-info-circle';
  }

  severityFor(type: NotificationType): TagSeverity {
    switch (type) {
      case 'TASK_ASSIGNED':
        return 'success';
      case 'COMMENT_ADDED':
        return 'info';
      case 'PROJECT_UPDATED':
        return 'secondary';
      case 'INVITATION_ACCEPTED':
        return 'warn';
      case 'SUBSCRIPTION_CHANGED':
        return 'contrast';
      default:
        return 'info';
    }
  }

  private patchNotification(id: number, patch: Partial<INotification>): void {
    this.notifications.update(notifications =>
      notifications.map(notification => (notification.id === id ? { ...notification, ...patch } : notification)),
    );
  }
}
