import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { PatientService } from '../../../core/services/patient.service';
import { RescheduleRequestService } from '../../../core/services/reschedule-request.service';
import { EmergencyService } from '../../../core/services/emergency.service';
import { ResourceService } from '../../../core/services/resource.service';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';
import { AppointmentRequest, AppointmentResponse } from '../../../core/models/appointment.model';
import { PatientRegistrationResponse } from '../../../core/models/patient.model';
import { RescheduleRequestResponse } from '../../../core/models/reschedule-request.model';
import { AvailabilityResponse } from '../../../core/models/availability.model';
import { RoomResponse, MachineResponse } from '../../../core/models/resource.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NotificationBellComponent],
  templateUrl: './patient-dashboard.component.html',
  styleUrl: './patient-dashboard.component.css',
})
export class PatientDashboardComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private patientService = inject(PatientService);
  private rescheduleRequestService = inject(RescheduleRequestService);
  private emergencyService = inject(EmergencyService);
  private resourceService = inject(ResourceService);

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
  readonly rooms = signal<RoomResponse[]>([]);
  readonly machines = signal<MachineResponse[]>([]);

  // States
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly cancellingId = signal<number | null>(null);

  // Reschedule Request Modal State
  showRescheduleModal = false;
  selectedAppointment: AppointmentResponse | null = null;
  newRequestedStart = '';
  newRequestedEnd = '';
  rescheduleReason = '';
  readonly isSubmittingReschedule = signal(false);

  // Book Appointment Modal State
  showBookModal = false;
  isEmergencyBooking = false;
  bookRoomId: number | null = null;
  bookMachineId: number | null = null;
  bookStart = '';
  bookEnd = '';
  bookNotes = '';
  readonly isBooking = signal(false);
  readonly availability = signal<AvailabilityResponse | null>(null);
  readonly isCheckingAvailability = signal(false);

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      const tab = params['tab'];
      if (tab && ['appointments', 'reschedules', 'records'].includes(tab)) {
        this.activeTab = tab;
      } else if (!tab) {
        this.activeTab = 'appointments';
      }
    });
    this.loadPatientData();
    this.loadLookups();
  }

  switchTab(tab: 'appointments' | 'reschedules' | 'records') {
    this.activeTab = tab;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: tab === 'appointments' ? {} : { tab },
    });
  }

  loadLookups() {
    this.resourceService.getAllRooms().subscribe({
      next: (res) => this.rooms.set(res.data ?? []),
    });
    this.resourceService.getAllMachines().subscribe({
      next: (res) => this.machines.set(res.data ?? []),
    });
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
          .filter(
            (a) =>
              (a.status === 'SCHEDULED' || a.status === 'RESCHEDULED' || a.status === 'EMERGENCY') &&
              new Date(a.scheduledStart) >= now,
          )
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

  openBookModal(emergency: boolean = false) {
    this.isEmergencyBooking = emergency;
    this.bookRoomId = null;
    this.bookMachineId = null;

    const now = new Date();
    now.setMinutes(Math.ceil(now.getMinutes() / 15) * 15, 0, 0);
    const end = new Date(now.getTime() + 4 * 60 * 60 * 1000);

    this.bookStart = this.toLocalIsoString(now);
    this.bookEnd = this.toLocalIsoString(end);
    this.bookNotes = emergency ? 'Urgent Patient Emergency Dialysis Request' : '';
    this.availability.set(null);
    this.showBookModal = true;
  }

  setSlotDuration(hours: number) {
    if (!this.bookStart) return;
    const start = new Date(this.bookStart);
    const end = new Date(start.getTime() + hours * 60 * 60 * 1000);
    this.bookEnd = this.toLocalIsoString(end);
  }

  checkSlotAvailability() {
    if (!this.bookStart || !this.bookEnd) {
      this.errorMessage.set('Enter start and end time to check slot availability.');
      return;
    }
    this.isCheckingAvailability.set(true);
    this.appointmentService.checkAvailability(this.bookStart, this.bookEnd).subscribe({
      next: (res) => {
        this.availability.set(res.data ?? null);
        this.isCheckingAvailability.set(false);
      },
      error: (err: NormalizedError) => {
        this.errorMessage.set(err.message);
        this.isCheckingAvailability.set(false);
      },
    });
  }

  createAppointment() {
    if (!this.bookStart || !this.bookEnd) {
      this.errorMessage.set('Please select time slot.');
      return;
    }

    this.isBooking.set(true);
    const req: AppointmentRequest = {
      patientId: this.currentUser()?.id,
      roomId: this.bookRoomId || undefined,
      machineId: this.bookMachineId || undefined,
      scheduledStart: this.bookStart,
      scheduledEnd: this.bookEnd,
      isEmergency: this.isEmergencyBooking,
      notes: this.bookNotes || undefined,
    };

    const action$ = this.isEmergencyBooking
      ? this.emergencyService.createEmergencyAppointment(req)
      : this.appointmentService.createAppointment(req);

    action$.subscribe({
      next: () => {
        this.isBooking.set(false);
        this.showBookModal = false;
        this.successMessage.set(
          this.isEmergencyBooking
            ? 'Emergency dialysis session confirmed and clinician allocated.'
            : 'Dialysis appointment created successfully.'
        );
        this.loadPatientData();
      },
      error: (err: NormalizedError) => {
        this.isBooking.set(false);
        this.errorMessage.set(err.message);
      },
    });
  }

  cancelAppointment(appointment: AppointmentResponse) {
    if (
      !confirm(
        `Cancel your appointment on ${this.formatDate(appointment.scheduledStart)}?`,
      )
    ) {
      return;
    }

    this.cancellingId.set(appointment.id);

    this.appointmentService.cancelAppointment(appointment.id).subscribe({
      next: () => {
        this.myAppointments.update((list) =>
          list.map((a) =>
            a.id === appointment.id ? { ...a, status: 'CANCELLED' as const } : a,
          ),
        );
        this.cancellingId.set(null);
        this.successMessage.set(`Appointment #${appointment.id} cancelled.`);
      },
      error: (err: NormalizedError) => {
        this.errorMessage.set(err.message);
        this.cancellingId.set(null);
      },
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

  private toLocalIsoString(date: Date): string {
    const pad = (n: number) => (n < 10 ? '0' + n : n);
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
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
