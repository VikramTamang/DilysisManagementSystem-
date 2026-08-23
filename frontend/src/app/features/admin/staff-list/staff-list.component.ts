import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { StaffService } from '../../../core/services/staff.service';
import { StaffMember, StaffType } from '../../../core/models/staff.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';

@Component({
  selector: 'app-staff-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './staff-list.component.html',
  styleUrl: './staff-list.component.css',
})
export class StaffListComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private staffService = inject(StaffService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  readonly allStaff = signal<StaffMember[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal('');
  readonly updatingId = signal<number | null>(null);

  searchTerm = '';
  typeFilter: StaffType | 'ALL' = 'ALL';

  readonly filteredStaff = computed(() => {
    const term = this.searchTerm.trim().toLowerCase();
    return this.allStaff().filter((s) => {
      const matchesType = this.typeFilter === 'ALL' || s.staffType === this.typeFilter;
      const matchesTerm =
        !term || s.name.toLowerCase().includes(term) || s.email.toLowerCase().includes(term);
      return matchesType && matchesTerm;
    });
  });

  ngOnInit() {
    this.loadStaff();
  }

  loadStaff() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.staffService.getAllStaff().subscribe({
      next: (staff) => {
        this.allStaff.set(staff);
        this.isLoading.set(false);
      },
      error: (err: NormalizedError) => {
        this.errorMessage.set(err.message);
        this.isLoading.set(false);
      },
    });
  }

  setTypeFilter(type: StaffType | 'ALL') {
    this.typeFilter = type;
  }

  toggleStatus(member: StaffMember) {
    const nextStatus = member.accountStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    const action = nextStatus === 'SUSPENDED' ? 'deactivate' : 'activate';

    if (
      !confirm(`${action === 'deactivate' ? 'Deactivate' : 'Activate'} ${member.name}'s account?`)
    ) {
      return;
    }

    this.updatingId.set(member.id);

    const onDone = () => {
      this.allStaff.update((list) =>
        list.map((s) =>
          s.id === member.id && s.staffType === member.staffType
            ? { ...s, accountStatus: nextStatus }
            : s,
        ),
      );
      this.updatingId.set(null);
    };

    const onFail = (err: NormalizedError) => {
      this.errorMessage.set(err.message);
      this.updatingId.set(null);
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

  editStaff(member: StaffMember) {
    this.router.navigate([
      '/dashboard/admin/staff/edit',
      member.staffType.toLowerCase(),
      member.id,
    ]);
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
      .map((part) => part[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}
