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
import { AppointmentRequest, AppointmentResponse } from '../../../core/models/appointment.model';
import { PatientResponse } from '../../../core/models/patient.model';
import { RescheduleRequestResponse } from '../../../core/models/reschedule-request.model';
import { EmergencyReassignmentResponse } from '../../../core/models/emergency.model';
import { AvailabilityResponse } from '../../../core/models/availability.model';
import { RoomResponse, MachineResponse } from '../../../core/models/resource.model';
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
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private patientService = inject(PatientService);
  private rescheduleRequestService = inject(RescheduleRequestService);
  private emergencyService = inject(EmergencyService);
  private resourceService = inject(ResourceService);

  isSidebarExpanded = true;
  activeTab: 'sessions' | 'patients' | 'reschedules' | 'emergency' = 'sessions';

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // Live Metrics
  readonly todaySessionsCount = signal<number>(0);
  readonly totalPatientsCount = signal<number>(0);
  readonly pendingReschedulesCount = signal<number>(0);

  // Data lists
  readonly doctorAppointments = signal<AppointmentResponse[]>([]);
  readonly pendingRequests = signal<RescheduleRequestResponse[]>([]);
  readonly patients = signal<PatientResponse[]>([]);
  readonly rooms = signal<RoomResponse[]>([]);
  readonly machines = signal<MachineResponse[]>([]);

  // States
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly completingApptId = signal<number | null>(null);

  completeAppointment(id: number) {
    if (!confirm(`Mark dialysis session #${id} as COMPLETED?`)) {
      return;
    }
    this.completingApptId.set(id);
    this.appointmentService.completeAppointment(id).subscribe({
      next: () => {
        this.completingApptId.set(null);
        this.successMessage.set(`Dialysis session #${id} marked as COMPLETED.`);
        this.loadDoctorData();
      },
      error: (err: NormalizedError) => {
        this.completingApptId.set(null);
        this.errorMessage.set(err.message);
      },
    });
  }

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
    this.route.queryParams.subscribe((params) => {
      const tab = params['tab'];
      if (tab && ['sessions', 'patients', 'reschedules', 'emergency'].includes(tab)) {
        this.activeTab = tab;
      } else if (!tab) {
        this.activeTab = 'sessions';
      }
    });
    this.loadDoctorData();
    this.loadLookups();
  }

  switchTab(tab: 'sessions' | 'patients' | 'reschedules' | 'emergency') {
    this.activeTab = tab;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: tab === 'sessions' ? {} : { tab },
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
    this.bookNotes = emergency ? 'Urgent Nephrology Emergency Dialysis' : '';
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
        this.loadDoctorData();
      },
      error: (err: NormalizedError) => {
        this.isBooking.set(false);
        this.errorMessage.set(err.message);
      },
    });
  }

  private toLocalIsoString(date: Date): string {
    const pad = (n: number) => (n < 10 ? '0' + n : n);
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
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
