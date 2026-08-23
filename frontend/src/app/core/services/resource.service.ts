import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  MachineRequest,
  MachineResponse,
  MachineStatusRequest,
  RoomRequest,
  RoomResponse,
  RoomStatusRequest,
} from '../models/resource.model';

@Injectable({ providedIn: 'root' })
export class ResourceService {
  private readonly roomsUrl = `${environment.apiBaseUrl}/rooms`;
  private readonly machinesUrl = `${environment.apiBaseUrl}/machines`;

  constructor(private http: HttpClient) {}

  getAllRooms(): Observable<ApiResponse<RoomResponse[]>> {
    return this.http.get<ApiResponse<RoomResponse[]>>(this.roomsUrl);
  }

  getRoomById(id: number): Observable<ApiResponse<RoomResponse>> {
    return this.http.get<ApiResponse<RoomResponse>>(`${this.roomsUrl}/${id}`);
  }

  createRoom(request: RoomRequest): Observable<ApiResponse<RoomResponse>> {
    return this.http.post<ApiResponse<RoomResponse>>(this.roomsUrl, request);
  }

  updateRoom(id: number, request: RoomRequest): Observable<ApiResponse<RoomResponse>> {
    return this.http.put<ApiResponse<RoomResponse>>(`${this.roomsUrl}/${id}`, request);
  }

  updateRoomStatus(id: number, status: RoomStatusRequest): Observable<ApiResponse<RoomResponse>> {
    return this.http.patch<ApiResponse<RoomResponse>>(`${this.roomsUrl}/${id}/status`, status);
  }

  deleteRoom(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.roomsUrl}/${id}`);
  }

  getAllMachines(): Observable<ApiResponse<MachineResponse[]>> {
    return this.http.get<ApiResponse<MachineResponse[]>>(this.machinesUrl);
  }

  getMachineById(id: number): Observable<ApiResponse<MachineResponse>> {
    return this.http.get<ApiResponse<MachineResponse>>(`${this.machinesUrl}/${id}`);
  }

  createMachine(request: MachineRequest): Observable<ApiResponse<MachineResponse>> {
    return this.http.post<ApiResponse<MachineResponse>>(this.machinesUrl, request);
  }

  updateMachine(id: number, request: MachineRequest): Observable<ApiResponse<MachineResponse>> {
    return this.http.put<ApiResponse<MachineResponse>>(`${this.machinesUrl}/${id}`, request);
  }

  updateMachineStatus(
    id: number,
    status: MachineStatusRequest,
  ): Observable<ApiResponse<MachineResponse>> {
    return this.http.patch<ApiResponse<MachineResponse>>(`${this.machinesUrl}/${id}/status`, status);
  }

  deleteMachine(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.machinesUrl}/${id}`);
  }
}
