import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { PatientService } from '../../../core/services/patient.service';
import { RescheduleRequestService } from '../../../core/services/reschedule-request.service';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';
import { AppointmentResponse } from '../../../core/models/appointment.model';
import { PatientRegistrationResponse } from '../../../core/models/patient.model';
import { RescheduleRequestResponse } from '../../../core/models/reschedule-request.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NotificationBellComponent],
  templateUrl: './patient-dashboard.component.html',
  styleUrl: './patient-dashboard.component.css',
})
export class PatientDashboardComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private patientService = inject(PatientService);
  private rescheduleRequestService = inject(RescheduleRequestService);

  isSidebarExpanded = true;
  activeTab: 'appointments' | 'reschedules' | 'records' = 'appointments';

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // Live Metrics & Profile
  readonly nextSession = signal<AppointmentResponse | null>(null);
  readonly totalCompletedSessions = signal<number>(0);
  readonly patientProfile = signal<PatientRegistrationResponse | null>(null);

  // Data lists
  readonly myAppointments = signal<AppointmentResponse[]>([]);
  readonly myRescheduleRequests = signal<RescheduleRequestResponse[]>([]);

  // States
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  // Reschedule Request Modal State
  showRescheduleModal = false;
  selectedAppointment: AppointmentResponse | null = null;
  newRequestedStart = '';
  newRequestedEnd = '';
  rescheduleReason = '';
  readonly isSubmittingReschedule = signal(false);

  ngOnInit() {
    this.loadPatientData();
  }

  loadPatientData() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    // 1. Fetch Patient's appointments
    this.appointmentService.getAppointments().subscribe({
      next: (res) => {
        const appts = res.data ?? [];
        this.myAppointments.set(appts);

        // Find upcoming session
        const now = new Date();
        const upcoming = appts
          .filter((a) => (a.status === 'SCHEDULED' || a.status === 'RESCHEDULED') && new Date(a.scheduledStart) >= now)
          .sort((a, b) => new Date(a.scheduledStart).getTime() - new Date(b.scheduledStart).getTime());

        this.nextSession.set(upcoming.length > 0 ? upcoming[0] : null);

        const completed = appts.filter((a) => a.status === 'COMPLETED');
        this.totalCompletedSessions.set(completed.length);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });

    // 2. Fetch Patient Profile & Clinical Details
    this.patientService.getCurrentPatient().subscribe({
      next: (res) => this.patientProfile.set(res.data ?? null),
      error: () => {},
    });

    // 3. Fetch Patient's Reschedule Requests
    this.rescheduleRequestService.getRequestsForPatient().subscribe({
      next: (res) => this.myRescheduleRequests.set(res.data ?? []),
      error: () => {},
    });
  }

  openRescheduleModal(appt: AppointmentResponse) {
    this.selectedAppointment = appt;
    this.newRequestedStart = '';
    this.newRequestedEnd = '';
    this.rescheduleReason = '';
    this.showRescheduleModal = true;
  }

  submitRescheduleRequest() {
    if (!this.selectedAppointment || !this.newRequestedStart || !this.newRequestedEnd) {
      this.errorMessage.set('Please provide both the requested start and end time.');
      return;
    }

    this.isSubmittingReschedule.set(true);
    this.errorMessage.set('');

    this.rescheduleRequestService
      .createRequest({
        appointmentId: this.selectedAppointment.id,
        requestedStart: this.newRequestedStart,
        requestedEnd: this.newRequestedEnd,
        reason: this.rescheduleReason,
      })
      .subscribe({
        next: () => {
          this.isSubmittingReschedule.set(false);
          this.showRescheduleModal = false;
          this.successMessage.set('Reschedule request submitted. Medical staff will review it.');
          this.loadPatientData();
        },
        error: (err: NormalizedError) => {
          this.isSubmittingReschedule.set(false);
          this.errorMessage.set(err.message);
        },
      });
  }

  formatDate(iso: string | null | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
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
