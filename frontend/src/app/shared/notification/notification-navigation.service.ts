import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';

import { INotification } from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationNavigationService {
  private readonly router = inject(Router);

  navigate(notification: INotification): Promise<boolean> {
    const sourceId = notification.sourceId;

    if (notification.sourceType === 'PROJECT' && sourceId != null) {
      return this.router.navigate(['/projects', sourceId]);
    }

    if (notification.sourceType === 'INVITATION') {
      return this.router.navigate(['/app/organizations']);
    }

    if (notification.sourceType === 'SUBSCRIPTION') {
      return this.router.navigate(['/app/billing']);
    }

    return this.router.navigate(['/notifications']);
  }
}
