import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './doctor-dashboard.component.html',
  styleUrl: './doctor-dashboard.component.css',
})
export class DoctorDashboardComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // TODO: replace with real data from AppointmentController filtered by staffId
  metrics = [
    { label: "Today's Sessions", value: '—' },
    { label: 'Total Patients', value: '—' },
    { label: 'Pending Reschedules', value: '—' },
  ];

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
