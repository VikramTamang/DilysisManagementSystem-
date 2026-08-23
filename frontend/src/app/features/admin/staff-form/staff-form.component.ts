import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { StaffService } from '../../../core/services/staff.service';
import {
  DoctorRequest,
  DoctorResponse,
  NurseRequest,
  NurseResponse,
  StaffType,
} from '../../../core/models/staff.model';
import { NormalizedError } from '../../../core/interceptors/error.interceptor';
import { NotificationBellComponent } from '../../../components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-staff-form',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NotificationBellComponent],
  templateUrl: './staff-form.component.html',
  styleUrl: './staff-form.component.css',
})
export class StaffFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private staffService = inject(StaffService);

  isSidebarExpanded = true;

  readonly currentUser = this.authService.currentUser;
  readonly initials = computed(() => this.getInitials(this.currentUser()?.name));

  isEditMode = false;
  editId: number | null = null;
  staffType: StaffType = 'DOCTOR';

  name = '';
  email = '';
  password = '';
  phone = '';
  experienceYears: number | null = null;

  licenseNumber = '';
  specialization = '';
  consultationFee: number | null = null;

  qualification = '';
  shift = '';
  assignedDepartment = '';

  readonly isSubmitting = signal(false);
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');

  ngOnInit() {
    const typeParam = this.route.snapshot.paramMap.get('type');
    const idParam = this.route.snapshot.paramMap.get('id');

    if (typeParam && idParam) {
      this.isEditMode = true;
      this.editId = Number(idParam);
      this.staffType = typeParam.toUpperCase() === 'NURSE' ? 'NURSE' : 'DOCTOR';
      this.loadExisting();
    }
  }

  private loadExisting() {
    if (!this.editId) return;
    this.isLoading.set(true);

    if (this.staffType === 'DOCTOR') {
      this.staffService.getDoctorById(this.editId).subscribe({
        next: (res) => this.populateForm(res.data),
        error: (err: NormalizedError) => this.onLoadError(err),
      });
    } else {
      this.staffService.getNurseById(this.editId).subscribe({
        next: (res) => this.populateForm(res.data),
        error: (err: NormalizedError) => this.onLoadError(err),
      });
    }
  }

  private populateForm(data: DoctorResponse | NurseResponse | null) {
    this.isLoading.set(false);
    if (!data) return;

    this.name = data.name;
    this.email = data.email;
    this.phone = data.phone ?? '';
    this.experienceYears = data.experienceYears;

    if (this.staffType === 'DOCTOR' && 'licenseNumber' in data) {
      this.licenseNumber = data.licenseNumber ?? '';
      this.specialization = data.specialization ?? '';
      this.consultationFee = data.consultationFee;
    } else if (this.staffType === 'NURSE' && 'qualification' in data) {
      this.qualification = data.qualification ?? '';
      this.shift = data.shift ?? '';
      this.assignedDepartment = data.assignedDepartment ?? '';
    }
  }

  private onLoadError(err: NormalizedError) {
    this.isLoading.set(false);
    this.errorMessage.set(err.message);
  }

  selectType(type: StaffType) {
    if (this.isEditMode) return;
    this.staffType = type;
  }

  onSubmit() {
    this.errorMessage.set('');

    if (!this.name || !this.email || (!this.isEditMode && !this.password)) {
      this.errorMessage.set('Name, email, and password are required.');
      return;
    }

    this.isSubmitting.set(true);

    const onDone = () => {
      this.isSubmitting.set(false);
      this.router.navigate(['/dashboard/admin/staff']);
    };

    const onFail = (err: NormalizedError) => {
      this.isSubmitting.set(false);
      this.errorMessage.set(err.message);
    };

    if (this.staffType === 'DOCTOR') {
      const request: DoctorRequest = {
        name: this.name,
        email: this.email,
        password: this.password || undefined,
        phone: this.phone || undefined,
        licenseNumber: this.licenseNumber || undefined,
        specialization: this.specialization || undefined,
        consultationFee: this.consultationFee ?? undefined,
        experienceYears: this.experienceYears ?? undefined,
      };

      if (this.isEditMode) {
        this.staffService
          .updateDoctor(this.editId!, request)
          .subscribe({ next: onDone, error: onFail });
      } else {
        this.staffService.createDoctor(request).subscribe({ next: onDone, error: onFail });
      }
    } else {
      const request: NurseRequest = {
        name: this.name,
        email: this.email,
        password: this.password || undefined,
        phone: this.phone || undefined,
        qualification: this.qualification || undefined,
        shift: this.shift || undefined,
        assignedDepartment: this.assignedDepartment || undefined,
        experienceYears: this.experienceYears ?? undefined,
      };

      if (this.isEditMode) {
        this.staffService
          .updateNurse(this.editId!, request)
          .subscribe({ next: onDone, error: onFail });
      } else {
        this.staffService.createNurse(request).subscribe({ next: onDone, error: onFail });
      }
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
      .map((part) => part[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }
}
