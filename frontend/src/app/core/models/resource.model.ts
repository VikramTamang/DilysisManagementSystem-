export interface RoomResponse {
  id: number;
  roomNumber: string;
  status: 'AVAILABLE' | 'MAINTENANCE';
}

export interface RoomRequest {
  roomNumber: string;
}

export interface RoomStatusRequest {
  status: 'AVAILABLE' | 'MAINTENANCE';
}

export interface MachineResponse {
  id: number;
  serialNumber: string;
  status: 'AVAILABLE' | 'MAINTENANCE';
}

export interface MachineRequest {
  serialNumber: string;
}

export interface MachineStatusRequest {
  status: 'AVAILABLE' | 'MAINTENANCE';
}
