import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-nurse-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './nurse-dashboard.component.html',
  styleUrl: './nurse-dashboard.component.css',
})
export class NurseDashboardComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  // TODO: replace with real data once available
  metrics = [
    { label: 'Patient Check-ins', value: '—' },
    { label: "Today's Treatments", value: '—' },
    { label: 'Active Tasks', value: '—' },
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
