export type NotificationType =
  | 'TASK_ASSIGNED'
  | 'COMMENT_ADDED'
  | 'PROJECT_UPDATED'
  | 'INVITATION_ACCEPTED'
  | 'SUBSCRIPTION_CHANGED'
  | 'SYSTEM';

export type NotificationSourceType = 'PROJECT' | 'TASK' | 'COMMENT' | 'INVITATION' | 'SUBSCRIPTION' | 'SYSTEM';

export interface INotification {
  id: number;
  organizationId: number;
  recipientLogin: string;
  title: string;
  message: string;
  type: NotificationType;
  sourceType?: NotificationSourceType | null;
  sourceId?: number | null;
  isRead: boolean;
  createdAt: string;
}

export interface IUnreadNotificationCount {
  count: number;
}

export type NotificationReadStatusFilter = 'ALL' | 'UNREAD' | 'READ';
