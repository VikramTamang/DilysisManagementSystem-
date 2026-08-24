import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { PatientSummary } from '../models/patient-summary.model';
import { PatientRegistrationRequest, PatientRegistrationResponse, PatientRequest, PatientResponse } from '../models/patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly baseUrl = `${environment.apiBaseUrl}/patients`;

  constructor(private http: HttpClient) {}

  getAllPatients(): Observable<ApiResponse<PatientResponse[]>> {
    return this.http.get<ApiResponse<PatientResponse[]>>(this.baseUrl);
  }

  getPatientById(id: number): Observable<ApiResponse<PatientRegistrationResponse>> {
    return this.http.get<ApiResponse<PatientRegistrationResponse>>(`${this.baseUrl}/${id}`);
  }

  getCurrentPatient(): Observable<ApiResponse<PatientRegistrationResponse>> {
    return this.http.get<ApiResponse<PatientRegistrationResponse>>(`${this.baseUrl}/me`);
  }

  createPatient(
    request: PatientRegistrationRequest,
  ): Observable<ApiResponse<PatientRegistrationResponse>> {
    return this.http.post<ApiResponse<PatientRegistrationResponse>>(this.baseUrl, request);
  }

  updatePatient(
    id: number,
    request: Partial<PatientRegistrationRequest>,
  ): Observable<ApiResponse<PatientRegistrationResponse>> {
    return this.http.put<ApiResponse<PatientRegistrationResponse>>(`${this.baseUrl}/${id}`, request);
  }

  deletePatient(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
