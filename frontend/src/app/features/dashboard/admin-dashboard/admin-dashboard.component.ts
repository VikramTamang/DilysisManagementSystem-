import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
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
import { AppointmentResponse } from '../../../core/models/appointment.model';
import { StaffMember } from '../../../core/models/staff.model';
import {
  AuditLogResponse,
  StaffActivityResponse,
  UtilizationReportResponse,
} from '../../../core/models/report.model';
import { EmergencyReassignmentResponse } from '../../../core/models/emergency.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private patientService = inject(PatientService);
  private appointmentService = inject(AppointmentService);
  private resourceService = inject(ResourceService);
  private reportService = inject(ReportService);
  private emergencyService = inject(EmergencyService);
  private staffService = inject(StaffService);

  isSidebarExpanded = true;
  activeTab: 'overview' | 'resources' | 'reports' | 'emergency' = 'overview';

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // Live Metrics
  readonly totalPatients = signal<number>(0);
  readonly todayAppointmentsCount = signal<number>(0);
  readonly availableRoomsCount = signal<number>(0);
  readonly totalRoomsCount = signal<number>(0);
  readonly availableMachinesCount = signal<number>(0);
  readonly totalMachinesCount = signal<number>(0);

  // Data lists
  readonly recentAppointments = signal<AppointmentResponse[]>([]);
  readonly rooms = signal<RoomResponse[]>([]);
  readonly machines = signal<MachineResponse[]>([]);
  readonly staffList = signal<StaffMember[]>([]);
  readonly auditLogs = signal<AuditLogResponse[]>([]);
  readonly staffActivity = signal<StaffActivityResponse[]>([]);
  readonly utilizationReport = signal<UtilizationReportResponse | null>(null);

  // Loading & Error States
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  // Modals state
  showAddRoomModal = false;
  newRoomNumber = '';

  showAddMachineModal = false;
  newMachineSerial = '';

  // Emergency Form State
  emergencyStaffId: number | null = null;
  emergencyStart = '';
  emergencyEnd = '';
  emergencyReason = '';
  readonly emergencyResult = signal<EmergencyReassignmentResponse | null>(null);
  readonly isProcessingEmergency = signal(false);

  // Report Date Filters (default: last 7 days to today)
  reportStart = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  reportEnd = new Date().toISOString().split('T')[0];

  ngOnInit() {
    this.loadAllDashboardData();
  }

  loadAllDashboardData() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const todayStr = new Date().toISOString().split('T')[0];

    // 1. Fetch Patients
    this.patientService.getAllPatients().subscribe({
      next: (res) => this.totalPatients.set(res.data?.length ?? 0),
      error: () => {},
    });

    // 2. Fetch Today's Appointments
    this.appointmentService.getAppointments({ date: todayStr }).subscribe({
      next: (res) => {
        const appts = res.data ?? [];
        this.todayAppointmentsCount.set(appts.length);
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

    // 5. Fetch Staff for emergency dropdown
    this.staffService.getAllStaff().subscribe({
      next: (staff) => this.staffList.set(staff),
      error: () => {},
    });
  }

  switchTab(tab: 'overview' | 'resources' | 'reports' | 'emergency') {
    this.activeTab = tab;
    this.errorMessage.set('');
    this.successMessage.set('');

    if (tab === 'reports') {
      this.loadReports();
    } else if (tab === 'resources') {
      this.loadResources();
    }
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
        this.availableMachinesCount.set(res.data?.filter((m) => m.status === 'AVAILABLE').length ?? 0);
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
