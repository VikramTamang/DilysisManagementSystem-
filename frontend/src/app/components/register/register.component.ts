import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { NormalizedError } from '../../core/interceptors/error.interceptor';
import { PatientRegistrationRequest } from '../../core/models/patient.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  name = '';
  email = '';
  password = '';
  confirmPassword = '';
  phone = '';
  address = '';
  dateOfBirth = '';
  bloodGroup = '';

  showPassword = false;
  errorMessage = '';

  readonly isSubmitting = signal(false);
  readonly isSuccess = signal(false);

  constructor(
    private router: Router,
    private authService: AuthService,
  ) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.errorMessage = '';

    if (!this.name || !this.email || !this.password) {
      this.errorMessage = 'Name, email, and password are required.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    const request: PatientRegistrationRequest = {
      name: this.name,
      email: this.email,
      password: this.password,
      phone: this.phone || undefined,
      address: this.address || undefined,
      dateOfBirth: this.dateOfBirth || undefined,
      bloodGroup: this.bloodGroup || undefined,
    };

    this.isSubmitting.set(true);

    this.authService.register(request).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.isSuccess.set(true);
      },
      error: (err: NormalizedError) => {
        this.isSubmitting.set(false);
        this.errorMessage = err.message;
      },
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
