import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { PatientSummary } from '../models/patient-summary.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly baseUrl = `${environment.apiBaseUrl}/patients`;

  constructor(private http: HttpClient) {}

  getAllPatients(): Observable<ApiResponse<PatientSummary[]>> {
    return this.http.get<ApiResponse<PatientSummary[]>>(this.baseUrl);
  }
}
