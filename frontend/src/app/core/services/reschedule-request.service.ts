import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  RescheduleRequestCreateRequest,
  RescheduleRequestResponse,
  RescheduleRequestReviewRequest,
  RescheduleRequestStatus,
} from '../models/reschedule-request.model';

@Injectable({ providedIn: 'root' })
export class RescheduleRequestService {
  private readonly baseUrl = `${environment.apiBaseUrl}/reschedule-requests`;

  constructor(private http: HttpClient) {}

  createRequest(
    request: RescheduleRequestCreateRequest,
  ): Observable<ApiResponse<RescheduleRequestResponse>> {
    return this.http.post<ApiResponse<RescheduleRequestResponse>>(this.baseUrl, request);
  }

  getAllRequests(
    status?: RescheduleRequestStatus,
  ): Observable<ApiResponse<RescheduleRequestResponse[]>> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<ApiResponse<RescheduleRequestResponse[]>>(this.baseUrl, { params });
  }

  getRequestsForPatient(patientId?: number): Observable<ApiResponse<RescheduleRequestResponse[]>> {
    return this.http.get<ApiResponse<RescheduleRequestResponse[]>>(this.baseUrl);
  }

  getRequestById(id: number): Observable<ApiResponse<RescheduleRequestResponse>> {
    return this.http.get<ApiResponse<RescheduleRequestResponse>>(`${this.baseUrl}/${id}`);
  }

  approveRequest(
    id: number,
    review?: RescheduleRequestReviewRequest,
  ): Observable<ApiResponse<RescheduleRequestResponse>> {
    return this.http.patch<ApiResponse<RescheduleRequestResponse>>(
      `${this.baseUrl}/${id}/approve`,
      review ?? {},
    );
  }

  rejectRequest(
    id: number,
    review?: RescheduleRequestReviewRequest,
  ): Observable<ApiResponse<RescheduleRequestResponse>> {
    return this.http.patch<ApiResponse<RescheduleRequestResponse>>(
      `${this.baseUrl}/${id}/reject`,
      review ?? {},
    );
  }
}
