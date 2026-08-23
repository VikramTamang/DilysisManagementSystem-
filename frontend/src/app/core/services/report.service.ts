import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  AuditLogResponse,
  StaffActivityResponse,
  UtilizationReportResponse,
} from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly baseUrl = `${environment.apiBaseUrl}/reports`;

  constructor(private http: HttpClient) {}

  getUtilizationReport(start: string, end: string): Observable<ApiResponse<UtilizationReportResponse>> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.http.get<ApiResponse<UtilizationReportResponse>>(`${this.baseUrl}/utilization`, { params });
  }

  getStaffActivityReport(start: string, end: string): Observable<ApiResponse<StaffActivityResponse[]>> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.http.get<ApiResponse<StaffActivityResponse[]>>(`${this.baseUrl}/staff-activity`, { params });
  }

  getAuditLogs(): Observable<ApiResponse<AuditLogResponse[]>> {
    return this.http.get<ApiResponse<AuditLogResponse[]>>(`${this.baseUrl}/audit-logs`);
  }
}
