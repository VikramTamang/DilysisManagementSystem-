import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { PatientService } from '../../../core/services/patient.service';
import { StaffService } from '../../../core/services/staff.service';
import { ResourceService } from '../../../core/services/resource.service';
import { EmergencyService } from '../../../core/services/emergency.service';
import { AppointmentRequest, AppointmentResponse, AppointmentStatus, RescheduleAppointmentRequest } from '../../../core/models/appointment.model';
import { PatientResponse } from '../../../core/models/patient.model';
import { StaffMember } from '../../../core/models/staff.model';
import { RoomResponse, MachineResponse } from '../../../core/models/resource.model';
import { AvailabilityResponse } from '../../../core/models/availability.model';
import { AuditLogResponse } from '../../../core/models/report.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';
import { getDashboardPathForRole } from '../../../core/utils/dashboard-route.util';
import { Role } from '../../../core/models/enums';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NotificationBellComponent],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.css',
})
export class AppointmentListComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);
  private patientService = inject(PatientService);
  private staffService = inject(StaffService);
  private resourceService = inject(ResourceService);
  private emergencyService = inject(EmergencyService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));
  readonly userRole = computed(() => this.currentUser()?.role);
  readonly dashboardPath = computed(() => {
    const role = this.currentUser()?.role;
    return role ? getDashboardPathForRole(role) : '/login';
  });
  readonly canManage = computed(() => {
    const role = this.currentUser()?.role;
    return role === Role.ADMIN || role === Role.DOCTOR || role === Role.NURSE;
  });

  // Data lists
  readonly appointments = signal<AppointmentResponse[]>([]);
  readonly patients = signal<PatientResponse[]>([]);
  readonly staffList = signal<StaffMember[]>([]);
  readonly rooms = signal<RoomResponse[]>([]);
  readonly machines = signal<MachineResponse[]>([]);

  // States
  readonly isLoading = signal(true);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly cancellingId = signal<number | null>(null);

  // Filters
  statusFilter: AppointmentStatus | 'ALL' = 'ALL';
  dateFilter = '';
  searchTerm = '';

  // Book Appointment Modal State
  showBookModal = false;
  isEmergencyBooking = false;
  bookPatientId: number | null = null;
  bookStaffId: number | null = null;
  bookRoomId: number | null = null;
  bookMachineId: number | null = null;
  bookStart = '';
  bookEnd = '';
  bookNotes = '';
  readonly isBooking = signal(false);
  readonly availability = signal<AvailabilityResponse | null>(null);
  readonly isCheckingAvailability = signal(false);

  // Direct Reschedule Modal State
  showRescheduleModal = false;
  selectedAppointment: AppointmentResponse | null = null;
  rescheduleStart = '';
  rescheduleEnd = '';
  rescheduleRoomId: number | null = null;
  rescheduleMachineId: number | null = null;
  readonly isRescheduling = signal(false);

  // Audit History Modal State
  showHistoryModal = false;
  selectedApptId: number | null = null;
  readonly historyLogs = signal<AuditLogResponse[]>([]);
  readonly isLoadingHistory = signal(false);

  readonly filteredAppointments = computed(() => {
    const term = this.searchTerm.trim().toLowerCase();
    return this.appointments().filter((a) => {
      if (!term) return true;
      return (
        a.patientName.toLowerCase().includes(term) ||
        a.staffName.toLowerCase().includes(term) ||
        a.roomNumber.toLowerCase().includes(term) ||
        a.machineSerialNumber.toLowerCase().includes(term) ||
        a.id.toString().includes(term)
      );
    });
  });

  ngOnInit() {
    this.loadAppointments();
    if (this.canManage()) {
      this.loadLookupData();
    }
  }

  loadLookupData() {
    this.patientService.getAllPatients().subscribe({
      next: (res) => this.patients.set(res.data ?? []),
    });
    this.staffService.getAllStaff().subscribe({
      next: (staff) => this.staffList.set(staff),
    });
    this.resourceService.getAllRooms().subscribe({
      next: (res) => this.rooms.set(res.data ?? []),
    });
    this.resourceService.getAllMachines().subscribe({
      next: (res) => this.machines.set(res.data ?? []),
    });
  }

  loadAppointments() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.appointmentService
      .getAppointments({
        status: this.statusFilter !== 'ALL' ? this.statusFilter : undefined,
        date: this.dateFilter || undefined,
      })
      .subscribe({
        next: (res) => {
          this.appointments.set(res.data ?? []);
          this.isLoading.set(false);
        },
        error: (err: NormalizedError) => {
          this.errorMessage.set(err.message);
          this.isLoading.set(false);
        },
      });
  }

  onFilterChange() {
    this.loadAppointments();
  }

  openBookModal(emergency: boolean = false) {
    this.isEmergencyBooking = emergency;
    this.bookPatientId = this.userRole() === Role.PATIENT ? (this.currentUser()?.id ?? null) : null;
    this.bookStaffId = null;
    this.bookRoomId = null;
    this.bookMachineId = null;

    const now = new Date();
    now.setMinutes(Math.ceil(now.getMinutes() / 15) * 15, 0, 0);
    const end = new Date(now.getTime() + 4 * 60 * 60 * 1000);

    this.bookStart = this.toLocalIsoString(now);
    this.bookEnd = this.toLocalIsoString(end);
    this.bookNotes = emergency ? 'Urgent Dialysis Emergency Session' : '';
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
      this.errorMessage.set('Start and end time are required.');
      return;
    }

    if (this.userRole() !== Role.PATIENT && !this.bookPatientId) {
      this.errorMessage.set('Please select a patient.');
      return;
    }

    this.isBooking.set(true);
    const req: AppointmentRequest = {
      patientId: this.bookPatientId || (this.userRole() === Role.PATIENT ? this.currentUser()?.id : undefined),
      staffId: this.bookStaffId || undefined,
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
            ? 'Emergency dialysis session confirmed and resources allocated.'
            : 'Dialysis appointment created successfully.'
        );
        this.loadAppointments();
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

  openRescheduleModal(appt: AppointmentResponse) {
    this.selectedAppointment = appt;
    this.rescheduleStart = appt.scheduledStart;
    this.rescheduleEnd = appt.scheduledEnd;
    this.rescheduleRoomId = appt.roomId;
    this.rescheduleMachineId = appt.machineId;
    this.showRescheduleModal = true;
  }

  submitDirectReschedule() {
    if (!this.selectedAppointment || !this.rescheduleStart || !this.rescheduleEnd) return;

    this.isRescheduling.set(true);
    const req: RescheduleAppointmentRequest = {
      scheduledStart: this.rescheduleStart,
      scheduledEnd: this.rescheduleEnd,
      roomId: this.rescheduleRoomId || undefined,
      machineId: this.rescheduleMachineId || undefined,
    };

    this.appointmentService.rescheduleAppointment(this.selectedAppointment.id, req).subscribe({
      next: () => {
        this.isRescheduling.set(false);
        this.showRescheduleModal = false;
        this.successMessage.set(`Appointment #${this.selectedAppointment?.id} rescheduled successfully.`);
        this.loadAppointments();
      },
      error: (err: NormalizedError) => {
        this.isRescheduling.set(false);
        this.errorMessage.set(err.message);
      },
    });
  }

  openHistoryModal(appt: AppointmentResponse) {
    this.selectedApptId = appt.id;
    this.showHistoryModal = true;
    this.isLoadingHistory.set(true);
    this.appointmentService.getAppointmentHistory(appt.id).subscribe({
      next: (res) => {
        this.historyLogs.set(res.data ?? []);
        this.isLoadingHistory.set(false);
      },
      error: () => this.isLoadingHistory.set(false),
    });
  }

  cancelAppointment(appointment: AppointmentResponse) {
    if (
      !confirm(
        `Cancel the appointment for ${appointment.patientName} on ${this.formatDate(appointment.scheduledStart)}?`,
      )
    ) {
      return;
    }

    this.cancellingId.set(appointment.id);

    this.appointmentService.cancelAppointment(appointment.id).subscribe({
      next: () => {
        this.appointments.update((list) =>
          list.map((a) =>
            a.id === appointment.id ? { ...a, status: 'CANCELLED' as AppointmentStatus } : a,
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
