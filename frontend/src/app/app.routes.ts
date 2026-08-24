import { Routes } from '@angular/router';
import { LandingComponent } from './components/landing/landing.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { AdminDashboardComponent } from './features/dashboard/admin-dashboard/admin-dashboard.component';
import { DoctorDashboardComponent } from './features/dashboard/doctor-dashboard/doctor-dashboard.component';
import { NurseDashboardComponent } from './features/dashboard/nurse-dashboard/nurse-dashboard.component';
import { PatientDashboardComponent } from './features/dashboard/patient-dashboard/patient-dashboard.component';
import { StaffListComponent } from './features/admin/staff-list/staff-list.component';
import { StaffFormComponent } from './features/admin/staff-form/staff-form.component';
import { AppointmentListComponent } from './features/appointments/appointment-list/appointment-list.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Role } from './core/models/enums';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'dashboard/admin',
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: [Role.ADMIN] },
  },
  {
    path: 'dashboard/admin/staff',
    redirectTo: () => '/dashboard/admin?tab=staff',
  },
  {
    path: 'dashboard/admin/staff/new',
    redirectTo: () => '/dashboard/admin?tab=staff',
  },
  {
    path: 'dashboard/admin/staff/edit/:type/:id',
    redirectTo: () => '/dashboard/admin?tab=staff',
  },
  {
    path: 'appointments',
    redirectTo: () => '/dashboard/admin?tab=appointments',
  },
  {
    path: 'dashboard/doctor',
    component: DoctorDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: [Role.DOCTOR] },
  },
  {
    path: 'dashboard/nurse',
    component: NurseDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: [Role.NURSE] },
  },
  {
    path: 'dashboard/patient',
    component: PatientDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: [Role.PATIENT] },
  },
  { path: '**', redirectTo: '' },
];
