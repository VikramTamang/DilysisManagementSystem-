import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ResourceService } from '../../../core/services/resource.service';
import { ReportService } from '../../../core/services/report.service';
import { EmergencyService } from '../../../core/services/emergency.service';
import { StaffService } from '../../../core/services/staff.service';
import { RoomResponse, MachineResponse } from '../../../core/models/resource.model';
import {
  AppointmentRequest,
  AppointmentResponse,
  AppointmentStatus,
  RescheduleAppointmentRequest,
} from '../../../core/models/appointment.model';
import { PatientResponse } from '../../../core/models/patient.model';
import {
  DoctorRequest,
  DoctorResponse,
  NurseRequest,
  NurseResponse,
  StaffMember,
  StaffType,
} from '../../../core/models/staff.model';
import { AvailabilityResponse } from '../../../core/models/availability.model';
import {
  AuditLogResponse,
  StaffActivityResponse,
  UtilizationReportResponse,
} from '../../../core/models/report.model';
import { EmergencyReassignmentResponse } from '../../../core/models/emergency.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NotificationBellComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private patientService = inject(PatientService);
  private appointmentService = inject(AppointmentService);
  private resourceService = inject(ResourceService);
  private reportService = inject(ReportService);
  private emergencyService = inject(EmergencyService);
  private staffService = inject(StaffService);

  isSidebarExpanded = true;
  activeTab: 'overview' | 'appointments' | 'staff' | 'patients' | 'resources' | 'reports' | 'emergency' = 'overview';

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  readonly appointmentStatusFilterOptions: (AppointmentStatus | 'ALL')[] = [
    'ALL',
    'SCHEDULED',
    'RESCHEDULED',
    'COMPLETED',
    'CANCELLED',
    'EMERGENCY',
  ];

  asDoctor(raw: DoctorResponse | NurseResponse): DoctorResponse {
    return raw as DoctorResponse;
  }

  asNurse(raw: DoctorResponse | NurseResponse): NurseResponse {
    return raw as NurseResponse;
  }

  setApptStatusFilter(st: AppointmentStatus | 'ALL') {
    this.appointmentStatusFilter.set(st);
  }

  // Live Metrics
  readonly totalPatients = signal<number>(0);
  readonly todayAppointmentsCount = signal<number>(0);
  readonly availableRoomsCount = signal<number>(0);
  readonly totalRoomsCount = signal<number>(0);
  readonly availableMachinesCount = signal<number>(0);
  readonly totalMachinesCount = signal<number>(0);
  readonly activeStaffCount = signal<number>(0);

  // Common Lookups
  readonly patientList = signal<PatientResponse[]>([]);
  readonly rooms = signal<RoomResponse[]>([]);
  readonly machines = signal<MachineResponse[]>([]);
  readonly staffList = signal<StaffMember[]>([]);
  readonly recentAppointments = signal<AppointmentResponse[]>([]);

  // TAB: APPOINTMENTS
  readonly allAppointments = signal<AppointmentResponse[]>([]);
  readonly appointmentStatusFilter = signal<AppointmentStatus | 'ALL'>('ALL');
  readonly appointmentDateFilter = signal<string>('');
  readonly appointmentSearchTerm = signal<string>('');
  readonly cancellingApptId = signal<number | null>(null);

  readonly filteredAppointments = computed(() => {
    const list = this.allAppointments();
    const term = this.appointmentSearchTerm().trim().toLowerCase();
    const status = this.appointmentStatusFilter();
    const date = this.appointmentDateFilter();

    return list.filter((a) => {
      const matchesStatus = status === 'ALL' || a.status === status;
      const matchesDate = !date || a.scheduledStart.startsWith(date);
      const matchesTerm =
        !term ||
        (a.patientName && a.patientName.toLowerCase().includes(term)) ||
        (a.staffName && a.staffName.toLowerCase().includes(term)) ||
        (a.roomNumber && a.roomNumber.toLowerCase().includes(term)) ||
        (a.machineSerialNumber && a.machineSerialNumber.toLowerCase().includes(term)) ||
        String(a.id).includes(term);

      return matchesStatus && matchesDate && matchesTerm;
    });
  });

  // TAB: MEDICAL STAFF
  readonly allStaff = signal<StaffMember[]>([]);
  readonly staffTypeFilter = signal<StaffType | 'ALL'>('ALL');
  readonly staffSearchTerm = signal<string>('');
  readonly updatingStaffId = signal<number | null>(null);

  readonly filteredStaff = computed(() => {
    const list = this.allStaff();
    const type = this.staffTypeFilter();
    const term = this.staffSearchTerm().trim().toLowerCase();

    return list.filter((s) => {
      const matchesType = type === 'ALL' || s.staffType === type;
      const matchesTerm =
        !term ||
        s.name.toLowerCase().includes(term) ||
        s.email.toLowerCase().includes(term) ||
        (s.phone && s.phone.toLowerCase().includes(term));

      return matchesType && matchesTerm;
    });
  });

  // TAB: PATIENTS
  readonly patientSearchTerm = signal<string>('');
  readonly patientBloodFilter = signal<string>('ALL');
  readonly patientSchedulingFilter = signal<'ALL' | 'SCHEDULED' | 'UNSCHEDULED'>('ALL');

  readonly filteredPatients = computed(() => {
    const list = this.patientList();
    const search = this.patientSearchTerm().toLowerCase().trim();
    const blood = this.patientBloodFilter();
    const sched = this.patientSchedulingFilter();

    return list.filter((p) => {
      const matchesSearch =
        !search ||
        p.name.toLowerCase().includes(search) ||
        p.email.toLowerCase().includes(search) ||
        (p.phone && p.phone.toLowerCase().includes(search)) ||
        (p.bloodGroup && p.bloodGroup.toLowerCase().includes(search));

      const matchesBlood = blood === 'ALL' || p.bloodGroup === blood;
      const matchesSched =
        sched === 'ALL' ||
        (sched === 'SCHEDULED' && p.schedulingStatus === 'SCHEDULED') ||
        (sched === 'UNSCHEDULED' && p.schedulingStatus !== 'SCHEDULED');

      return matchesSearch && matchesBlood && matchesSched;
    });
  });

  // TAB: REPORTS & AUDIT
  readonly auditLogs = signal<AuditLogResponse[]>([]);
  readonly staffActivity = signal<StaffActivityResponse[]>([]);
  readonly utilizationReport = signal<UtilizationReportResponse | null>(null);
  reportStart = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  reportEnd = new Date().toISOString().split('T')[0];

  // Loading & Error States
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  // Modals state
  showAddRoomModal = false;
  newRoomNumber = '';

  showAddMachineModal = false;
  newMachineSerial = '';

  // Universal Book Appointment Modal State
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

  readonly isSlotAvailable = computed(() => {
    const av = this.availability();
    if (!av) return false;
    const hasRoom = av.rooms && av.rooms.some((r) => r.available);
    const hasMachine = av.machines && av.machines.some((m) => m.available);
    return Boolean(hasRoom && hasMachine);
  });

  readonly availableRoomsCountFromCheck = computed(() => {
    return this.availability()?.rooms?.filter((r) => r.available).length || 0;
  });

  readonly availableMachinesCountFromCheck = computed(() => {
    return this.availability()?.machines?.filter((m) => m.available).length || 0;
  });

  getStaffAvailability(staffId: number) {
    return this.availability()?.staff?.find((s) => s.id === staffId);
  }

  isDoctorOccupied(staffId: number | null): boolean {
    if (!staffId || !this.availability()) return false;
    const s = this.getStaffAvailability(staffId);
    return s ? !s.available : false;
  }

  // Completing appointment state
  readonly completingApptId = signal<number | null>(null);

  // Direct Reschedule Modal State
  showRescheduleModal = false;
  selectedRescheduleAppt: AppointmentResponse | null = null;
  rescheduleStart = '';
  rescheduleEnd = '';
  rescheduleRoomId: number | null = null;
  rescheduleMachineId: number | null = null;
  readonly isRescheduling = signal(false);

  // Appt History Modal State
  showApptHistoryModal = false;
  selectedApptHistoryId: number | null = null;
  readonly apptHistoryLogs = signal<AuditLogResponse[]>([]);
  readonly isLoadingApptHistory = signal(false);

  // Staff Modal State (Add & Edit)
  showStaffModal = false;
  isStaffEditMode = false;
  editStaffId: number | null = null;
  staffModalType: StaffType = 'DOCTOR';
  staffName = '';
  staffEmail = '';
  staffPassword = '';
  staffPhone = '';
  staffExpYears: number | null = null;
  doctorLicense = '';
  doctorSpecialization = '';
  doctorFee: number | null = null;
  nurseQualification = '';
  nurseShift = '';
  nurseDept = '';
  readonly isSavingStaff = signal(false);

  // Emergency Form State
  emergencyStaffId: number | null = null;
  emergencyStart = '';
  emergencyEnd = '';
  emergencyReason = '';
  readonly emergencyResult = signal<EmergencyReassignmentResponse | null>(null);
  readonly isProcessingEmergency = signal(false);

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      const tab = params['tab'];
      if (
        tab &&
        ['overview', 'appointments', 'staff', 'patients', 'resources', 'reports', 'emergency'].includes(
          tab
        )
      ) {
        this.switchTab(tab, false);
      } else if (!tab) {
        this.switchTab('overview', false);
      }
    });
    this.loadAllDashboardData();
  }

  loadAllDashboardData() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const todayStr = new Date().toISOString().split('T')[0];

    // 1. Fetch Patients
    this.patientService.getAllPatients().subscribe({
      next: (res) => {
        const pList = res.data ?? [];
        this.patientList.set(pList);
        this.totalPatients.set(pList.length);
      },
      error: () => {},
    });

    // 2. Fetch Appointments
    this.appointmentService.getAppointments({}).subscribe({
      next: (res) => {
        const appts = res.data ?? [];
        this.allAppointments.set(appts);
        const todayAppts = appts.filter(
          (a) => a.scheduledStart.startsWith(todayStr) && a.status !== 'CANCELLED'
        );
        this.todayAppointmentsCount.set(todayAppts.length);
        this.recentAppointments.set(appts.slice(0, 5));
      },
      error: () => {},
    });

    // 3. Fetch Rooms
    this.resourceService.getAllRooms().subscribe({
      next: (res) => {
        const rList = res.data ?? [];
        this.rooms.set(rList);
        this.totalRoomsCount.set(rList.length);
        this.availableRoomsCount.set(rList.filter((r) => r.status === 'AVAILABLE').length);
      },
      error: () => {},
    });

    // 4. Fetch Machines
    this.resourceService.getAllMachines().subscribe({
      next: (res) => {
        const mList = res.data ?? [];
        this.machines.set(mList);
        this.totalMachinesCount.set(mList.length);
        this.availableMachinesCount.set(mList.filter((m) => m.status === 'AVAILABLE').length);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });

    // 5. Fetch Staff
    this.staffService.getAllStaff().subscribe({
      next: (staff) => {
        this.staffList.set(staff);
        this.allStaff.set(staff);
        this.activeStaffCount.set(staff.filter((s) => s.accountStatus === 'ACTIVE').length);
      },
      error: () => {},
    });
  }

  switchTab(
    tab: 'overview' | 'appointments' | 'staff' | 'patients' | 'resources' | 'reports' | 'emergency',
    updateUrl = true
  ) {
    if (this.activeTab === tab && !updateUrl) {
      return;
    }
    this.activeTab = tab;
    this.errorMessage.set('');
    this.successMessage.set('');

    if (updateUrl) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: tab === 'overview' ? {} : { tab },
      });
    }

    if (tab === 'appointments' && this.allAppointments().length === 0) {
      this.loadAppointments();
    } else if (tab === 'staff' && this.allStaff().length === 0) {
      this.loadStaff();
    } else if (tab === 'reports' && this.auditLogs().length === 0) {
      this.loadReports();
    } else if (tab === 'resources' && this.rooms().length === 0) {
      this.loadResources();
    }
  }

  loadAppointments() {
    this.appointmentService.getAppointments({}).subscribe({
      next: (res) => {
        this.allAppointments.set(res.data ?? []);
      },
      error: (err: NormalizedError) => this.errorMessage.set(err.message),
    });
  }

  cancelAppointment(id: number) {
    if (!confirm(`Are you sure you want to cancel Appointment #${id}?`)) {
      return;
    }
    this.cancellingApptId.set(id);
    this.appointmentService.cancelAppointment(id).subscribe({
      next: () => {
        this.cancellingApptId.set(null);
        this.successMessage.set(`Appointment #${id} has been cancelled.`);
        this.loadAppointments();
        this.loadAllDashboardData();
      },
      error: (err: NormalizedError) => {
        this.cancellingApptId.set(null);
        this.errorMessage.set(err.message);
      },
    });
  }

  completeAppointment(id: number) {
    if (!confirm(`Mark dialysis session for Appointment #${id} as COMPLETED?`)) {
      return;
    }
    this.completingApptId.set(id);
    this.appointmentService.completeAppointment(id).subscribe({
      next: () => {
        this.completingApptId.set(null);
        this.successMessage.set(`Appointment #${id} has been successfully completed.`);
        this.loadAppointments();
        this.loadAllDashboardData();
      },
      error: (err: NormalizedError) => {
        this.completingApptId.set(null);
        this.errorMessage.set(err.message);
      },
    });
  }

  openRescheduleModal(appt: AppointmentResponse) {
    this.selectedRescheduleAppt = appt;
    this.rescheduleStart = appt.scheduledStart ? appt.scheduledStart.slice(0, 16) : '';
    this.rescheduleEnd = appt.scheduledEnd ? appt.scheduledEnd.slice(0, 16) : '';
    this.rescheduleRoomId = appt.roomId || null;
    this.rescheduleMachineId = appt.machineId || null;
    this.showRescheduleModal = true;
  }

  submitReschedule() {
    if (!this.selectedRescheduleAppt || !this.rescheduleStart || !this.rescheduleEnd) {
      this.errorMessage.set('Enter new start and end time.');
      return;
    }
    this.isRescheduling.set(true);

    const req: RescheduleAppointmentRequest = {
      scheduledStart: this.rescheduleStart,
      scheduledEnd: this.rescheduleEnd,
      roomId: this.rescheduleRoomId || undefined,
      machineId: this.rescheduleMachineId || undefined,
    };

    this.appointmentService.rescheduleAppointment(this.selectedRescheduleAppt.id, req).subscribe({
      next: () => {
        this.isRescheduling.set(false);
        this.showRescheduleModal = false;
        this.successMessage.set(
          `Appointment #${this.selectedRescheduleAppt!.id} rescheduled successfully.`
        );
        this.loadAppointments();
      },
      error: (err: NormalizedError) => {
        this.isRescheduling.set(false);
        this.errorMessage.set(err.message);
      },
    });
  }

  openApptHistoryModal(appt: AppointmentResponse) {
    this.selectedApptHistoryId = appt.id;
    this.showApptHistoryModal = true;
    this.isLoadingApptHistory.set(true);

    this.reportService.getAuditLogs().subscribe({
      next: (res) => {
        const logs = (res.data ?? []).filter((l) => l.appointmentId === appt.id);
        this.apptHistoryLogs.set(logs);
        this.isLoadingApptHistory.set(false);
      },
      error: () => {
        this.apptHistoryLogs.set([]);
        this.isLoadingApptHistory.set(false);
      },
    });
  }

  loadStaff() {
    this.staffService.getAllStaff().subscribe({
      next: (staff) => {
        this.allStaff.set(staff);
        this.staffList.set(staff);
        this.activeStaffCount.set(staff.filter((s) => s.accountStatus === 'ACTIVE').length);
      },
      error: (err: NormalizedError) => this.errorMessage.set(err.message),
    });
  }

  toggleStaffStatus(member: StaffMember) {
    const nextStatus = member.accountStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    const action = nextStatus === 'SUSPENDED' ? 'deactivate' : 'activate';

    if (!confirm(`${action === 'deactivate' ? 'Deactivate' : 'Activate'} ${member.name}'s account?`)) {
      return;
    }

    this.updatingStaffId.set(member.id);

    const onDone = () => {
      this.allStaff.update((list) =>
        list.map((s) =>
          s.id === member.id && s.staffType === member.staffType
            ? { ...s, accountStatus: nextStatus }
            : s
        )
      );
      this.updatingStaffId.set(null);
      this.successMessage.set(`${member.name}'s account is now ${nextStatus}.`);
    };

    const onFail = (err: NormalizedError) => {
      this.errorMessage.set(err.message);
      this.updatingStaffId.set(null);
    };

    if (member.staffType === 'DOCTOR') {
      this.staffService.updateDoctorStatus(member.id, { accountStatus: nextStatus }).subscribe({
        next: onDone,
        error: onFail,
      });
    } else {
      this.staffService.updateNurseStatus(member.id, { accountStatus: nextStatus }).subscribe({
        next: onDone,
        error: onFail,
      });
    }
  }

  openAddStaffModal(type: StaffType = 'DOCTOR') {
    this.isStaffEditMode = false;
    this.editStaffId = null;
    this.staffModalType = type;
    this.staffName = '';
    this.staffEmail = '';
    this.staffPassword = '';
    this.staffPhone = '';
    this.staffExpYears = null;
    this.doctorLicense = '';
    this.doctorSpecialization = 'Nephrology';
    this.doctorFee = 1500;
    this.nurseQualification = 'B.Sc Nursing (Dialysis Certified)';
    this.nurseShift = 'Morning';
    this.nurseDept = 'Dialysis Unit';
    this.showStaffModal = true;
  }

  openEditStaffModal(member: StaffMember) {
    this.isStaffEditMode = true;
    this.editStaffId = member.id;
    this.staffModalType = member.staffType;
    this.staffName = member.name;
    this.staffEmail = member.email;
    this.staffPassword = '';
    this.staffPhone = member.phone || '';
    this.staffExpYears = member.experienceYears || null;
    this.showStaffModal = true;

    if (member.staffType === 'DOCTOR') {
      this.staffService.getDoctorById(member.id).subscribe({
        next: (res) => {
          if (res.data) {
            this.doctorLicense = res.data.licenseNumber || '';
            this.doctorSpecialization = res.data.specialization || 'Nephrology';
            this.doctorFee = res.data.consultationFee || 1500;
          }
        },
      });
    } else {
      this.staffService.getNurseById(member.id).subscribe({
        next: (res) => {
          if (res.data) {
            this.nurseQualification = res.data.qualification || '';
            this.nurseShift = res.data.shift || 'Morning';
            this.nurseDept = res.data.assignedDepartment || 'Dialysis Unit';
          }
        },
      });
    }
  }

  saveStaff() {
    if (!this.staffName.trim() || !this.staffEmail.trim()) {
      this.errorMessage.set('Name and Email are required.');
      return;
    }
    this.isSavingStaff.set(true);

    if (this.staffModalType === 'DOCTOR') {
      const docReq: DoctorRequest = {
        name: this.staffName.trim(),
        email: this.staffEmail.trim(),
        password: this.staffPassword || (this.isStaffEditMode ? undefined : 'Doctor@12345'),
        phone: this.staffPhone || undefined,
        experienceYears: this.staffExpYears ?? undefined,
        licenseNumber: this.doctorLicense || undefined,
        specialization: this.doctorSpecialization || undefined,
        consultationFee: this.doctorFee ?? undefined,
      };

      const op$ =
        this.isStaffEditMode && this.editStaffId
          ? this.staffService.updateDoctor(this.editStaffId, docReq)
          : this.staffService.createDoctor(docReq);

      op$.subscribe({
        next: () => {
          this.isSavingStaff.set(false);
          this.showStaffModal = false;
          this.successMessage.set(
            `Doctor ${this.isStaffEditMode ? 'updated' : 'created'} successfully.`
          );
          this.loadStaff();
        },
        error: (err: NormalizedError) => {
          this.errorMessage.set(err.message);
          this.isSavingStaff.set(false);
        },
      });
    } else {
      const nurseReq: NurseRequest = {
        name: this.staffName.trim(),
        email: this.staffEmail.trim(),
        password: this.staffPassword || (this.isStaffEditMode ? undefined : 'Nurse@12345'),
        phone: this.staffPhone || undefined,
        experienceYears: this.staffExpYears ?? undefined,
        qualification: this.nurseQualification || undefined,
        shift: this.nurseShift || undefined,
        assignedDepartment: this.nurseDept || undefined,
      };

      const op$ =
        this.isStaffEditMode && this.editStaffId
          ? this.staffService.updateNurse(this.editStaffId, nurseReq)
          : this.staffService.createNurse(nurseReq);

      op$.subscribe({
        next: () => {
          this.isSavingStaff.set(false);
          this.showStaffModal = false;
          this.successMessage.set(
            `Nurse ${this.isStaffEditMode ? 'updated' : 'created'} successfully.`
          );
          this.loadStaff();
        },
        error: (err: NormalizedError) => {
          this.errorMessage.set(err.message);
          this.isSavingStaff.set(false);
        },
      });
    }
  }

  openBookModal(emergency: boolean = false) {
    this.isEmergencyBooking = emergency;
    this.bookPatientId = null;
    this.bookStaffId = null;
    this.bookRoomId = null;
    this.bookMachineId = null;

    const now = new Date();
    now.setMinutes(Math.ceil(now.getMinutes() / 15) * 15, 0, 0);
    const end = new Date(now.getTime() + 4 * 60 * 60 * 1000);

    this.bookStart = this.toLocalIsoString(now);
    this.bookEnd = this.toLocalIsoString(end);
    this.bookNotes = emergency ? 'Admin Emergency Priority Dialysis' : '';
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
            ? 'Emergency dialysis session confirmed and allocated.'
            : 'Dialysis appointment created successfully.'
        );
        this.loadAllDashboardData();
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
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
      date.getHours()
    )}:${pad(date.getMinutes())}`;
  }

  loadResources() {
    this.resourceService.getAllRooms().subscribe({
      next: (res) => {
        this.rooms.set(res.data ?? []);
        this.totalRoomsCount.set(res.data?.length ?? 0);
        this.availableRoomsCount.set(res.data?.filter((r) => r.status === 'AVAILABLE').length ?? 0);
      },
    });
    this.resourceService.getAllMachines().subscribe({
      next: (res) => {
        this.machines.set(res.data ?? []);
        this.totalMachinesCount.set(res.data?.length ?? 0);
        this.availableMachinesCount.set(
          res.data?.filter((m) => m.status === 'AVAILABLE').length ?? 0
        );
      },
    });
  }

  toggleRoomStatus(room: RoomResponse) {
    const nextStatus = room.status === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE';
    this.resourceService.updateRoomStatus(room.id, { status: nextStatus }).subscribe({
      next: () => {
        this.successMessage.set(`Room ${room.roomNumber} set to ${nextStatus}`);
        this.loadResources();
      },
      error: (err: NormalizedError) => this.errorMessage.set(err.message),
    });
  }

  toggleMachineStatus(machine: MachineResponse) {
    const nextStatus = machine.status === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE';
    this.resourceService.updateMachineStatus(machine.id, { status: nextStatus }).subscribe({
      next: () => {
        this.successMessage.set(`Machine ${machine.serialNumber} set to ${nextStatus}`);
        this.loadResources();
      },
      error: (err: NormalizedError) => this.errorMessage.set(err.message),
    });
  }

  createRoom() {
    if (!this.newRoomNumber.trim()) return;
    this.resourceService.createRoom({ roomNumber: this.newRoomNumber.trim() }).subscribe({
      next: () => {
        this.newRoomNumber = '';
        this.showAddRoomModal = false;
        this.successMessage.set('New Room added successfully');
        this.loadResources();
      },
      error: (err: NormalizedError) => this.errorMessage.set(err.message),
    });
  }

  createMachine() {
    if (!this.newMachineSerial.trim()) return;
    this.resourceService.createMachine({ serialNumber: this.newMachineSerial.trim() }).subscribe({
      next: () => {
        this.newMachineSerial = '';
        this.showAddMachineModal = false;
        this.successMessage.set('New Dialysis Machine added successfully');
        this.loadResources();
      },
      error: (err: NormalizedError) => this.errorMessage.set(err.message),
    });
  }

  loadReports() {
    this.isLoading.set(true);
    this.reportService.getUtilizationReport(this.reportStart, this.reportEnd).subscribe({
      next: (res) => {
        this.utilizationReport.set(res.data ?? null);
        this.isLoading.set(false);
      },
      error: (err: NormalizedError) => {
        this.errorMessage.set(err.message);
        this.isLoading.set(false);
      },
    });

    this.reportService.getStaffActivityReport(this.reportStart, this.reportEnd).subscribe({
      next: (res) => this.staffActivity.set(res.data ?? []),
    });

    this.reportService.getAuditLogs().subscribe({
      next: (res) => this.auditLogs.set((res.data ?? []).slice(0, 30)),
    });
  }

  submitEmergencyLeave() {
    if (!this.emergencyStaffId || !this.emergencyStart || !this.emergencyEnd) {
      this.errorMessage.set('Please select staff member and date-time window.');
      return;
    }

    this.isProcessingEmergency.set(true);
    this.errorMessage.set('');
    this.emergencyResult.set(null);

    this.emergencyService
      .declareStaffUnavailable({
        staffId: this.emergencyStaffId,
        unavailableStart: this.emergencyStart,
        unavailableEnd: this.emergencyEnd,
        reason: this.emergencyReason,
      })
      .subscribe({
        next: (res) => {
          this.emergencyResult.set(res.data ?? null);
          this.isProcessingEmergency.set(false);
          this.successMessage.set('Emergency reassignment process completed.');
          this.loadAllDashboardData();
        },
        error: (err: NormalizedError) => {
          this.errorMessage.set(err.message);
          this.isProcessingEmergency.set(false);
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
