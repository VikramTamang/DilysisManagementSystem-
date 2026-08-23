import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { DelayNotificationRequest, NotificationItem } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly baseUrl = `${environment.apiBaseUrl}/notifications`;

  constructor(private http: HttpClient) {}

  getMyNotifications(): Observable<ApiResponse<NotificationItem[]>> {
    return this.http.get<ApiResponse<NotificationItem[]>>(this.baseUrl);
  }

  getUnreadCount(): Observable<ApiResponse<{ unreadCount: number }>> {
    return this.http.get<ApiResponse<{ unreadCount: number }>>(`${this.baseUrl}/unread-count`);
  }

  markAsRead(id: number): Observable<ApiResponse<NotificationItem>> {
    return this.http.patch<ApiResponse<NotificationItem>>(`${this.baseUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.baseUrl}/read-all`, {});
  }

  sendDelayNotice(request: DelayNotificationRequest): Observable<ApiResponse<NotificationItem>> {
    return this.http.post<ApiResponse<NotificationItem>>(`${this.baseUrl}/delay`, request);
  }
}
