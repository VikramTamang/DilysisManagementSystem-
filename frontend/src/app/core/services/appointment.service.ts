import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  AppointmentFilters,
  AppointmentRequest,
  AppointmentResponse,
  RescheduleAppointmentRequest,
} from '../models/appointment.model';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly baseUrl = `${environment.apiBaseUrl}/appointments`;

  constructor(private http: HttpClient) {}

  getAppointments(filters?: AppointmentFilters): Observable<ApiResponse<AppointmentResponse[]>> {
    let params = new HttpParams();
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.staffId != null) params = params.set('staffId', filters.staffId);
    if (filters?.date) params = params.set('date', filters.date);

    return this.http.get<ApiResponse<AppointmentResponse[]>>(this.baseUrl, { params });
  }

  getAppointmentById(id: number): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.get<ApiResponse<AppointmentResponse>>(`${this.baseUrl}/${id}`);
  }

  createAppointment(request: AppointmentRequest): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.post<ApiResponse<AppointmentResponse>>(this.baseUrl, request);
  }

  rescheduleAppointment(
    id: number,
    request: RescheduleAppointmentRequest,
  ): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.patch<ApiResponse<AppointmentResponse>>(
      `${this.baseUrl}/${id}/reschedule`,
      request,
    );
  }

  cancelAppointment(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
