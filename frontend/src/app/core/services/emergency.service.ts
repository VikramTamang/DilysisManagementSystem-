import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  EmergencyReassignmentResponse,
  StaffUnavailabilityRequest,
} from '../models/emergency.model';

@Injectable({ providedIn: 'root' })
export class EmergencyService {
  private readonly baseUrl = `${environment.apiBaseUrl}/emergency`;

  constructor(private http: HttpClient) {}

  declareStaffUnavailable(
    request: StaffUnavailabilityRequest,
  ): Observable<ApiResponse<EmergencyReassignmentResponse>> {
    return this.http.post<ApiResponse<EmergencyReassignmentResponse>>(
      `${this.baseUrl}/staff-unavailable`,
      request,
    );
  }
}
