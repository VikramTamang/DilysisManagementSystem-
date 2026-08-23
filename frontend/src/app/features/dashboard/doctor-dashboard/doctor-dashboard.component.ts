import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { RescheduleRequestService } from '../../../core/services/reschedule-request.service';
import { EmergencyService } from '../../../core/services/emergency.service';
import { AppointmentResponse } from '../../../core/models/appointment.model';
import { RescheduleRequestResponse } from '../../../core/models/reschedule-request.model';
import { EmergencyReassignmentResponse } from '../../../core/models/emergency.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NotificationBellComponent],
  templateUrl: './doctor-dashboard.component.html',
  styleUrl: './doctor-dashboard.component.css',
})
export class DoctorDashboardComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private rescheduleRequestService = inject(RescheduleRequestService);
  private emergencyService = inject(EmergencyService);

  isSidebarExpanded = true;
  activeTab: 'sessions' | 'reschedules' | 'emergency' = 'sessions';

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // Live Metrics
  readonly todaySessionsCount = signal<number>(0);
  readonly totalPatientsCount = signal<number>(0);
  readonly pendingReschedulesCount = signal<number>(0);

  // Data lists
  readonly doctorAppointments = signal<AppointmentResponse[]>([]);
  readonly pendingRequests = signal<RescheduleRequestResponse[]>([]);

  // States
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  // Review modal / action state
  selectedRequest: RescheduleRequestResponse | null = null;
  reviewNote = '';
  isReviewing = false;

  // Emergency Leave State
  emergencyStart = '';
  emergencyEnd = '';
  emergencyReason = '';
  readonly isSubmittingEmergency = signal(false);
  readonly emergencyResult = signal<EmergencyReassignmentResponse | null>(null);

  ngOnInit() {
    this.loadDoctorData();
  }

  loadDoctorData() {
    const user = this.currentUser();
    if (!user) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    // Fetch Doctor's Appointments
    this.appointmentService.getAppointments({ staffId: user.id }).subscribe({
      next: (res) => {
        const appts = res.data ?? [];
        this.doctorAppointments.set(appts);

        const todayStr = new Date().toISOString().split('T')[0];
        const todayAppts = appts.filter(
          (a) => a.scheduledStart.startsWith(todayStr) && a.status !== 'CANCELLED',
        );
        this.todaySessionsCount.set(todayAppts.length);

        const uniquePatients = new Set(appts.map((a) => a.patientId));
        this.totalPatientsCount.set(uniquePatients.size);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });

    // Fetch Pending Reschedule Requests
    this.rescheduleRequestService.getAllRequests('PENDING').subscribe({
      next: (res) => {
        const reqs = res.data ?? [];
        this.pendingRequests.set(reqs);
        this.pendingReschedulesCount.set(reqs.length);
      },
      error: () => {},
    });
  }

  openReviewModal(req: RescheduleRequestResponse) {
    this.selectedRequest = req;
    this.reviewNote = '';
  }

  approveRequest(req: RescheduleRequestResponse) {
    this.isReviewing = true;
    this.rescheduleRequestService.approveRequest(req.id, { reviewNote: this.reviewNote }).subscribe({
      next: () => {
        this.isReviewing = false;
        this.selectedRequest = null;
        this.successMessage.set(`Reschedule request #${req.id} approved successfully.`);
        this.loadDoctorData();
      },
      error: (err: NormalizedError) => {
        this.isReviewing = false;
        this.errorMessage.set(err.message);
      },
    });
  }

  rejectRequest(req: RescheduleRequestResponse) {
    this.isReviewing = true;
    this.rescheduleRequestService.rejectRequest(req.id, { reviewNote: this.reviewNote }).subscribe({
      next: () => {
        this.isReviewing = false;
        this.selectedRequest = null;
        this.successMessage.set(`Reschedule request #${req.id} rejected.`);
        this.loadDoctorData();
      },
      error: (err: NormalizedError) => {
        this.isReviewing = false;
        this.errorMessage.set(err.message);
      },
    });
  }

  submitDoctorEmergency() {
    const user = this.currentUser();
    if (!user || !this.emergencyStart || !this.emergencyEnd) {
      this.errorMessage.set('Please select the start and end time for emergency leave.');
      return;
    }

    this.isSubmittingEmergency.set(true);
    this.errorMessage.set('');

    this.emergencyService
      .declareStaffUnavailable({
        staffId: user.id,
        unavailableStart: this.emergencyStart,
        unavailableEnd: this.emergencyEnd,
        reason: this.emergencyReason || 'Doctor Emergency Leave',
      })
      .subscribe({
        next: (res) => {
          this.emergencyResult.set(res.data ?? null);
          this.isSubmittingEmergency.set(false);
          this.successMessage.set('Emergency leave registered. Patients reassigned.');
          this.loadDoctorData();
        },
        error: (err: NormalizedError) => {
          this.errorMessage.set(err.message);
          this.isSubmittingEmergency.set(false);
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
