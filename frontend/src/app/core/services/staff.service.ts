import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  DoctorRequest,
  DoctorResponse,
  NurseRequest,
  NurseResponse,
  StaffMember,
  StaffStatusRequest,
} from '../models/staff.model';

@Injectable({ providedIn: 'root' })
export class StaffService {
  private readonly doctorsUrl = `${environment.apiBaseUrl}/doctors`;
  private readonly nursesUrl = `${environment.apiBaseUrl}/nurses`;

  constructor(private http: HttpClient) {}

  getAllDoctors(): Observable<ApiResponse<DoctorResponse[]>> {
    return this.http.get<ApiResponse<DoctorResponse[]>>(this.doctorsUrl);
  }

  getAllNurses(): Observable<ApiResponse<NurseResponse[]>> {
    return this.http.get<ApiResponse<NurseResponse[]>>(this.nursesUrl);
  }

  /** Combines doctors + nurses into one sorted list for the staff table. */
  getAllStaff(): Observable<StaffMember[]> {
    return forkJoin({
      doctors: this.getAllDoctors(),
      nurses: this.getAllNurses(),
    }).pipe(
      map(({ doctors, nurses }) => {
        const doctorMembers: StaffMember[] = (doctors.data ?? []).map((d) => ({
          id: d.id,
          name: d.name,
          email: d.email,
          phone: d.phone,
          experienceYears: d.experienceYears,
          accountStatus: d.accountStatus,
          staffType: 'DOCTOR' as const,
          raw: d,
        }));

        const nurseMembers: StaffMember[] = (nurses.data ?? []).map((n) => ({
          id: n.id,
          name: n.name,
          email: n.email,
          phone: n.phone,
          experienceYears: n.experienceYears,
          accountStatus: n.accountStatus,
          staffType: 'NURSE' as const,
          raw: n,
        }));

        return [...doctorMembers, ...nurseMembers].sort((a, b) => a.name.localeCompare(b.name));
      }),
    );
  }

  getDoctorById(id: number): Observable<ApiResponse<DoctorResponse>> {
    return this.http.get<ApiResponse<DoctorResponse>>(`${this.doctorsUrl}/${id}`);
  }

  getNurseById(id: number): Observable<ApiResponse<NurseResponse>> {
    return this.http.get<ApiResponse<NurseResponse>>(`${this.nursesUrl}/${id}`);
  }

  createDoctor(request: DoctorRequest): Observable<ApiResponse<DoctorResponse>> {
    return this.http.post<ApiResponse<DoctorResponse>>(this.doctorsUrl, request);
  }

  createNurse(request: NurseRequest): Observable<ApiResponse<NurseResponse>> {
    return this.http.post<ApiResponse<NurseResponse>>(this.nursesUrl, request);
  }

  updateDoctor(id: number, request: DoctorRequest): Observable<ApiResponse<DoctorResponse>> {
    return this.http.put<ApiResponse<DoctorResponse>>(`${this.doctorsUrl}/${id}`, request);
  }

  updateNurse(id: number, request: NurseRequest): Observable<ApiResponse<NurseResponse>> {
    return this.http.put<ApiResponse<NurseResponse>>(`${this.nursesUrl}/${id}`, request);
  }

  updateDoctorStatus(
    id: number,
    status: StaffStatusRequest,
  ): Observable<ApiResponse<DoctorResponse>> {
    return this.http.patch<ApiResponse<DoctorResponse>>(`${this.doctorsUrl}/${id}/status`, status);
  }

  updateNurseStatus(
    id: number,
    status: StaffStatusRequest,
  ): Observable<ApiResponse<NurseResponse>> {
    return this.http.patch<ApiResponse<NurseResponse>>(`${this.nursesUrl}/${id}/status`, status);
  }
}
