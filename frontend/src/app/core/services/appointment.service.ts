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
import { AvailabilityResponse } from '../models/availability.model';
import { AuditLogResponse } from '../models/report.model';

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

  updateAppointment(
    id: number,
    request: AppointmentRequest,
  ): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.put<ApiResponse<AppointmentResponse>>(`${this.baseUrl}/${id}`, request);
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

  checkAvailability(
    start: string,
    end: string,
    date?: string,
  ): Observable<ApiResponse<AvailabilityResponse>> {
    let params = new HttpParams().set('start', start).set('end', end);
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get<ApiResponse<AvailabilityResponse>>(`${this.baseUrl}/availability`, {
      params,
    });
  }

  getAppointmentHistory(id: number): Observable<ApiResponse<AuditLogResponse[]>> {
    return this.http.get<ApiResponse<AuditLogResponse[]>>(`${this.baseUrl}/${id}/history`);
  }
}
