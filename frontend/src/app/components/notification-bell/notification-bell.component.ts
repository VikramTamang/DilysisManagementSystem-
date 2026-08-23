import {
  Component,
  ElementRef,
  HostListener,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationItem, NotificationType } from '../../core/models/notification.model';
import { NormalizedError } from '../../core/interceptors/error.interceptor';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.css',
})
export class NotificationBellComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private elementRef = inject(ElementRef);
  private router = inject(Router);

  readonly notifications = signal<NotificationItem[]>([]);
  readonly unreadCount = signal(0);
  readonly isOpen = signal(false);
  readonly isLoading = signal(false);

  readonly hasUnread = computed(() => this.unreadCount() > 0);

  ngOnInit() {
    this.loadUnreadCount();
  }

  toggleDropdown() {
    this.isOpen.update((open) => !open);
    if (this.isOpen()) {
      this.loadNotifications();
    }
  }

  @HostListener('document:click', ['$event'])
  onOutsideClick(event: MouseEvent) {
    if (this.isOpen() && !this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }

  loadUnreadCount() {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => this.unreadCount.set(res.data?.unreadCount ?? 0),
      error: () => {
        // Silently fail on the badge count — a stale/missing count shouldn't block the rest of the dashboard.
      },
    });
  }

  loadNotifications() {
    this.isLoading.set(true);
    this.notificationService.getMyNotifications().subscribe({
      next: (res) => {
        this.notifications.set(res.data ?? []);
        this.isLoading.set(false);
      },
      error: (err: NormalizedError) => {
        console.error('Failed to load notifications:', err.message);
        this.isLoading.set(false);
      },
    });
  }

  markAsRead(notification: NotificationItem, event: Event) {
    event.stopPropagation();
    if (notification.read) return;

    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications.update((list) =>
          list.map((n) => (n.id === notification.id ? { ...n, read: true } : n)),
        );
        this.unreadCount.update((count) => Math.max(0, count - 1));
      },
    });
  }

  markAllAsRead() {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update((list) => list.map((n) => ({ ...n, read: true })));
        this.unreadCount.set(0);
      },
    });
  }

  iconFor(type: NotificationType): string {
    switch (type) {
      case 'APPOINTMENT_CONFIRMED':
        return 'check';
      case 'APPOINTMENT_RESCHEDULED':
        return 'clock';
      case 'APPOINTMENT_CANCELLED':
        return 'x';
      case 'APPOINTMENT_DELAYED':
        return 'alert';
      default:
        return 'bell';
    }
  }

  timeAgo(isoDate: string): string {
    const diffMs = Date.now() - new Date(isoDate).getTime();
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1) return 'just now';
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return `${days}d ago`;
  }
}
