import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AppointmentResponse } from '../../../core/models/appointment.model';
import { DelayNotificationRequest } from '../../../core/models/notification.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';

@Component({
  selector: 'app-nurse-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './nurse-dashboard.component.html',
  styleUrl: './nurse-dashboard.component.css',
})
export class NurseDashboardComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private notificationService = inject(NotificationService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // Live Metrics
  readonly todayTreatmentsCount = signal<number>(0);
  readonly activeSessionsCount = signal<number>(0);
  readonly totalAssignedPatientsCount = signal<number>(0);

  // Data list
  readonly nurseAppointments = signal<AppointmentResponse[]>([]);

  // States
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  // Delay Notification Modal State
  showDelayModal = false;
  selectedAppointment: AppointmentResponse | null = null;
  delayMinutes = 15;
  delayReason = 'Dialysis machine maintenance and sanitization in progress';
  readonly isSendingDelay = signal(false);

  ngOnInit() {
    this.loadNurseData();
  }

  loadNurseData() {
    const user = this.currentUser();
    if (!user) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    const todayStr = new Date().toISOString().split('T')[0];

    this.appointmentService.getAppointments({ date: todayStr }).subscribe({
      next: (res) => {
        const appts = res.data ?? [];
        this.nurseAppointments.set(appts);
        this.todayTreatmentsCount.set(appts.length);
        this.activeSessionsCount.set(
          appts.filter((a) => a.status === 'SCHEDULED' || a.status === 'RESCHEDULED').length,
        );
        const patients = new Set(appts.map((a) => a.patientId));
        this.totalAssignedPatientsCount.set(patients.size);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  openDelayModal(appt: AppointmentResponse) {
    this.selectedAppointment = appt;
    this.delayMinutes = 15;
    this.delayReason = 'Dialysis machine sanitization/preparation in progress';
    this.showDelayModal = true;
  }

  sendDelayNotification() {
    if (!this.selectedAppointment) return;

    this.isSendingDelay.set(true);
    const req: DelayNotificationRequest = {
      patientId: this.selectedAppointment.patientId,
      appointmentId: this.selectedAppointment.id,
      delayMinutes: this.delayMinutes,
      reason: this.delayReason,
    };

    this.notificationService.sendDelayNotice(req).subscribe({
      next: () => {
        this.isSendingDelay.set(false);
        this.showDelayModal = false;
        this.successMessage.set(
          `Delay notification (${this.delayMinutes} min) sent to patient ${this.selectedAppointment?.patientName}.`,
        );
      },
      error: (err: NormalizedError) => {
        this.isSendingDelay.set(false);
        this.errorMessage.set(err.message);
      },
    });
  }

  formatDate(iso: string): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  }

  toggleSidebar() {
    this.isSidebarExpanded = !this.isSidebarExpanded;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  private getInitials(name: string | undefined): string {
    if (!name) return '?';
    return name
      .split(' ')
      .map((p) => p[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}
