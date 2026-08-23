import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { AuthenticatedUser, LoginRequest, LoginResponse } from '../models/auth.model';
import { PatientRegistrationRequest, PatientRegistrationResponse } from '../models/patient.model';

const TOKEN_KEY = 'gateway_access_token';
const USER_KEY = 'gateway_current_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly currentUserSignal = signal<AuthenticatedUser | null>(this.readStoredUser());

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  readonly userRole = computed(() => this.currentUserSignal()?.role ?? null);

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.baseUrl}/login`, credentials).pipe(
      tap((res) => {
        if (res.success && res.data) {
          this.persistSession(res.data);
        }
      }),
    );
  }

  register(
    request: PatientRegistrationRequest,
  ): Observable<ApiResponse<PatientRegistrationResponse>> {
    // No session is persisted here — registration doesn't return a token.
    // The user logs in separately via the existing login flow.
    return this.http.post<ApiResponse<PatientRegistrationResponse>>(
      `${this.baseUrl}/register`,
      request,
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private persistSession(loginResponse: LoginResponse): void {
    const user: AuthenticatedUser = {
      id: loginResponse.id,
      name: loginResponse.name,
      email: loginResponse.email,
      role: loginResponse.role,
      designation: loginResponse.designation,
    };

    localStorage.setItem(TOKEN_KEY, loginResponse.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private readStoredUser(): AuthenticatedUser | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthenticatedUser) : null;
  }
}
