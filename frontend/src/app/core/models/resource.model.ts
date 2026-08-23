export interface RoomResponse {
  id: number;
  roomNumber: string;
  status: 'AVAILABLE' | 'MAINTENANCE';
}

export interface MachineResponse {
  id: number;
  serialNumber: string;
  status: 'AVAILABLE' | 'MAINTENANCE';
}
