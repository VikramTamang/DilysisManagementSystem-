import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [NotificationBellComponent],
  templateUrl: './patient-dashboard.component.html',
  styleUrl: './patient-dashboard.component.css',
})
export class PatientDashboardComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // TODO: replace with real data once available
  metrics = [
    { label: 'Upcoming Session', value: '—' },
    { label: 'Assigned Doctor', value: '—' },
    { label: 'Assigned Nurse', value: '—' },
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
