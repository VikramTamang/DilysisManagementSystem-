import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // TODO: replace with real data once ReportController / RoomController services are wired
  metrics = [
    { label: 'Active Patients', value: '—' },
    { label: "Today's Appointments", value: '—' },
    { label: 'Available Rooms', value: '—' },
    { label: 'Available Machines', value: '—' },
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
      .map((part) => part[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}
