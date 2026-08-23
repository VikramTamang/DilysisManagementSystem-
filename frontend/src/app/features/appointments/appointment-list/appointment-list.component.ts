import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AppointmentResponse, AppointmentStatus } from '../../../core/models/appointment.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';
import { getDashboardPathForRole } from '../../../core/utils/dashboard-route.util';
import { Role } from '../../../core/models/enums';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.css',
})
export class AppointmentListComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private appointmentService = inject(AppointmentService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));
  readonly dashboardPath = computed(() => {
    const role = this.currentUser()?.role;
    return role ? getDashboardPathForRole(role) : '/login';
  });
  readonly isAdmin = computed(() => this.currentUser()?.role === Role.ADMIN);

  readonly appointments = signal<AppointmentResponse[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal('');
  readonly cancellingId = signal<number | null>(null);

  statusFilter: AppointmentStatus | 'ALL' = 'ALL';
  dateFilter = '';

  ngOnInit() {
    this.loadAppointments();
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
      },
      error: (err: NormalizedError) => {
        this.errorMessage.set(err.message);
        this.cancellingId.set(null);
      },
    });
  }

  editAppointment(appointment: AppointmentResponse) {
    this.router.navigate(['/appointments/edit', appointment.id]);
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  }

  statusBadgeClass(status: AppointmentStatus): string {
    switch (status) {
      case 'SCHEDULED':
        return 'text-blue-400 bg-blue-950';
      case 'RESCHEDULED':
        return 'text-amber-400 bg-amber-950';
      case 'CANCELLED':
        return 'text-rose-400 bg-rose-950';
      case 'COMPLETED':
        return 'text-emerald-400 bg-emerald-950';
      case 'PENDING_REASSIGNMENT':
        return 'text-orange-400 bg-orange-950';
      default:
        return 'text-gray-400 bg-gray-950';
    }
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
