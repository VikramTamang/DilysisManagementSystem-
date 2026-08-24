import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { NotificationService } from '../../../core/services/notification.service';
import { EmergencyService } from '../../../core/services/emergency.service';
import { PatientService } from '../../../core/services/patient.service';
import { ResourceService } from '../../../core/services/resource.service';
import { AppointmentRequest, AppointmentResponse } from '../../../core/models/appointment.model';
import { DelayNotificationRequest } from '../../../core/models/notification.model';
import { EmergencyReassignmentResponse } from '../../../core/models/emergency.model';
import { PatientResponse } from '../../../core/models/patient.model';
import { RoomResponse, MachineResponse } from '../../../core/models/resource.model';
import { AvailabilityResponse } from '../../../core/models/availability.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-nurse-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NotificationBellComponent],
  templateUrl: './nurse-dashboard.component.html',
  styleUrl: './nurse-dashboard.component.css',
})
export class NurseDashboardComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private notificationService = inject(NotificationService);
  private emergencyService = inject(EmergencyService);
  private patientService = inject(PatientService);
  private resourceService = inject(ResourceService);

  isSidebarExpanded = true;
  activeTab: 'overview' | 'emergency' = 'overview';

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // Live Metrics
  readonly todayTreatmentsCount = signal<number>(0);
  readonly activeSessionsCount = signal<number>(0);
  readonly totalAssignedPatientsCount = signal<number>(0);

  // Data list
  readonly nurseAppointments = signal<AppointmentResponse[]>([]);
  readonly patients = signal<PatientResponse[]>([]);
  readonly rooms = signal<RoomResponse[]>([]);
  readonly machines = signal<MachineResponse[]>([]);

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

  // Book Appointment Modal State
  showBookModal = false;
  isEmergencyBooking = false;
  bookPatientId: number | null = null;
  bookRoomId: number | null = null;
  bookMachineId: number | null = null;
  bookStart = '';
  bookEnd = '';
  bookNotes = '';
  readonly isBooking = signal(false);
  readonly availability = signal<AvailabilityResponse | null>(null);
  readonly isCheckingAvailability = signal(false);

  // Emergency Leave State
  emergencyStart = '';
  emergencyEnd = '';
  emergencyReason = '';
  readonly isSubmittingEmergency = signal(false);
  readonly emergencyResult = signal<EmergencyReassignmentResponse | null>(null);

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      const tab = params['tab'];
      if (tab && ['overview', 'emergency'].includes(tab)) {
        this.activeTab = tab;
      } else if (!tab) {
        this.activeTab = 'overview';
      }
    });
    this.loadNurseData();
    this.loadLookups();
  }

  switchTab(tab: 'overview' | 'emergency') {
    this.activeTab = tab;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: tab === 'overview' ? {} : { tab },
    });
  }

  loadLookups() {
    this.patientService.getAllPatients().subscribe({
      next: (res) => this.patients.set(res.data ?? []),
    });
    this.resourceService.getAllRooms().subscribe({
      next: (res) => this.rooms.set(res.data ?? []),
    });
    this.resourceService.getAllMachines().subscribe({
      next: (res) => this.machines.set(res.data ?? []),
    });
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
          appts.filter((a) => a.status === 'SCHEDULED' || a.status === 'RESCHEDULED' || a.status === 'EMERGENCY').length,
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
    const delayMsg = `Your dialysis session is delayed by approx ${this.delayMinutes} minutes. Reason: ${this.delayReason}. Please check in at the floor nurse station.`;

    const req: DelayNotificationRequest = {
      appointmentId: this.selectedAppointment.id,
      message: delayMsg,
    };

    this.notificationService.sendDelayNotice(req).subscribe({
      next: () => {
        this.isSendingDelay.set(false);
        this.showDelayModal = false;
        this.successMessage.set(
          `Delay alert (${this.delayMinutes} min) broadcasted to patient ${this.selectedAppointment?.patientName}.`,
        );
      },
      error: (err: NormalizedError) => {
        this.isSendingDelay.set(false);
        this.errorMessage.set(err.message);
      },
    });
  }

  openBookModal(emergency: boolean = false) {
    this.isEmergencyBooking = emergency;
    this.bookPatientId = null;
    this.bookRoomId = null;
    this.bookMachineId = null;

    const now = new Date();
    now.setMinutes(Math.ceil(now.getMinutes() / 15) * 15, 0, 0);
    const end = new Date(now.getTime() + 4 * 60 * 60 * 1000);

    this.bookStart = this.toLocalIsoString(now);
    this.bookEnd = this.toLocalIsoString(end);
    this.bookNotes = emergency ? 'Urgent Floor Emergency Dialysis Session' : '';
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
    if (!this.bookPatientId || !this.bookStart || !this.bookEnd) {
      this.errorMessage.set('Please select patient and time slot.');
      return;
    }

    this.isBooking.set(true);
    const req: AppointmentRequest = {
      patientId: this.bookPatientId,
      staffId: this.currentUser()?.id,
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
            ? 'Emergency dialysis session confirmed and allocated.'
            : 'Dialysis appointment created successfully.'
        );
        this.loadNurseData();
      },
      error: (err: NormalizedError) => {
        this.isBooking.set(false);
        this.errorMessage.set(err.message);
      },
    });
  }

  submitNurseEmergency() {
    const user = this.currentUser();
    if (!user || !this.emergencyStart || !this.emergencyEnd) {
      this.errorMessage.set('Please select start and end time.');
      return;
    }

    this.isSubmittingEmergency.set(true);
    this.errorMessage.set('');

    this.emergencyService
      .declareStaffUnavailable({
        staffId: user.id,
        unavailableStart: this.emergencyStart,
        unavailableEnd: this.emergencyEnd,
        reason: this.emergencyReason || 'Nurse Emergency Leave',
      })
      .subscribe({
        next: (res) => {
          this.emergencyResult.set(res.data ?? null);
          this.isSubmittingEmergency.set(false);
          this.successMessage.set('Emergency leave registered. Patients reassigned.');
          this.loadNurseData();
        },
        error: (err: NormalizedError) => {
          this.errorMessage.set(err.message);
          this.isSubmittingEmergency.set(false);
        },
      });
  }

  private toLocalIsoString(date: Date): string {
    const pad = (n: number) => (n < 10 ? '0' + n : n);
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
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
