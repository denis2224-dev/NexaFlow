import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { INotification, IUnreadNotificationCount } from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/notifications', 'notificationservice');

  getMyNotifications(page = 0, size = 20, sort = 'createdAt,desc'): Observable<HttpResponse<INotification[]>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<INotification[]>(`${this.resourceUrl}/my`, { params, observe: 'response' });
  }

  getLatestNotifications(limit = 5): Observable<INotification[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<INotification[]>(`${this.resourceUrl}/my/latest`, { params });
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number | IUnreadNotificationCount>(`${this.resourceUrl}/my/unread-count`).pipe(
      map(response => {
        if (typeof response === 'number') {
          return response;
        }
        return response.count;
      }),
    );
  }

  markAsRead(id: number): Observable<INotification> {
    return this.http.put<INotification>(`${this.resourceUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.resourceUrl}/my/read-all`, {});
  }
}
