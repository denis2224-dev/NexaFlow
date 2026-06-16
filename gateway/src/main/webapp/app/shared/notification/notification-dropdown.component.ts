import { DatePipe, NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { Popover, PopoverModule } from 'primeng/popover';
import { SkeletonModule } from 'primeng/skeleton';

import { extractNexaFlowErrorMessage } from 'app/core/nexaflow/nexaflow-error.util';
import { INotification, NotificationType } from './notification.model';
import { NotificationNavigationService } from './notification-navigation.service';
import { NotificationStateService } from './notification-state.service';

@Component({
  selector: 'jhi-notification-dropdown',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './notification-dropdown.component.html',
  styleUrl: './notification-dropdown.component.scss',
  imports: [RouterLink, DatePipe, NgClass, ButtonModule, PopoverModule, SkeletonModule],
})
export default class NotificationDropdownComponent {
  readonly state = inject(NotificationStateService);
  readonly badgeLabel = computed(() => {
    const count = this.state.unreadCount();
    return count > 99 ? '99+' : `${count}`;
  });

  private readonly notificationNavigationService = inject(NotificationNavigationService);
  private readonly messageService = inject(MessageService);

  toggle(event: Event, popover: Popover): void {
    popover.toggle(event);
    this.state.refreshLatestNotifications();
  }

  markAllAsRead(event: Event): void {
    event.stopPropagation();
    if (this.state.unreadCount() === 0) {
      return;
    }

    this.state.markAllAsRead().subscribe({
      next: () => {
        this.messageService.add({
          key: 'shell-notifications',
          severity: 'success',
          summary: 'Notifications updated',
          detail: 'All notifications marked as read.',
        });
      },
      error: error => {
        this.messageService.add({
          severity: 'error',
          key: 'shell-notifications',
          summary: 'Notifications not updated',
          detail: extractNexaFlowErrorMessage(error, 'Notifications could not be marked as read.'),
        });
      },
    });
  }

  openNotification(notification: INotification, popover: Popover): void {
    const navigate = (): void => {
      popover.hide();
      void this.notificationNavigationService.navigate(notification);
    };

    if (notification.isRead) {
      navigate();
      return;
    }

    this.state.markAsRead(notification).subscribe({
      next: navigate,
      error: error => {
        this.messageService.add({
          severity: 'error',
          key: 'shell-notifications',
          summary: 'Notification not updated',
          detail: extractNexaFlowErrorMessage(error, 'Notification could not be marked as read.'),
        });
      },
    });
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
}
