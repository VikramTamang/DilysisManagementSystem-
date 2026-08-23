import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TitleCasePipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { NormalizedError } from '../../core/interceptors/error.interceptor';
import { getDashboardPathForRole } from '../../core/utils/dashboard-route.util';

@Component({
  selector: 'app-login',
  imports: [RouterLink, FormsModule, TitleCasePipe],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  email = '';
  password = '';
  showPassword = false;
  selectedRole = 'patient';
  errorMessage = '';

  readonly isSubmitting = signal(false);

  constructor(
    private router: Router,
    private authService: AuthService,
  ) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  selectRole(role: string) {
    this.selectedRole = role;
  }

  onSubmit() {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please fill out all fields.';
      return;
    }

    this.errorMessage = '';
    this.isSubmitting.set(true);

    this.authService.login({ username: this.email, password: this.password }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        const role = this.authService.currentUser()?.role;
        this.router.navigate([role ? getDashboardPathForRole(role) : '/login']);
      },
      error: (err: NormalizedError) => {
        this.isSubmitting.set(false);
        this.errorMessage = err.message;
      },
    });
  }
}
