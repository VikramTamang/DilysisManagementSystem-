import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { MachineResponse, RoomResponse } from '../models/resource.model';

@Injectable({ providedIn: 'root' })
export class ResourceService {
  private readonly roomsUrl = `${environment.apiBaseUrl}/rooms`;
  private readonly machinesUrl = `${environment.apiBaseUrl}/machines`;

  constructor(private http: HttpClient) {}

  getAllRooms(): Observable<ApiResponse<RoomResponse[]>> {
    return this.http.get<ApiResponse<RoomResponse[]>>(this.roomsUrl);
  }

  getAllMachines(): Observable<ApiResponse<MachineResponse[]>> {
    return this.http.get<ApiResponse<MachineResponse[]>>(this.machinesUrl);
  }
}
