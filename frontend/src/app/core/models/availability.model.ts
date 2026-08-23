export interface RoomAvailability {
  id: number;
  roomNumber: string;
  available: boolean;
}

export interface MachineAvailability {
  id: number;
  serialNumber: string;
  available: boolean;
}

export interface StaffAvailability {
  id: number;
  name: string;
  email: string;
  available: boolean;
}

export interface AvailabilityResponse {
  rooms: RoomAvailability[];
  machines: MachineAvailability[];
  staff: StaffAvailability[];
}
