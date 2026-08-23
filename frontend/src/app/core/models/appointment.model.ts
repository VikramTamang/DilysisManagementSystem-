export type AppointmentStatus =
  'SCHEDULED' | 'RESCHEDULED' | 'CANCELLED' | 'COMPLETED' | 'PENDING_REASSIGNMENT';

export interface AppointmentResponse {
  id: number;
  patientId: number;
  patientName: string;
  staffId: number;
  staffName: string;
  roomId: number;
  roomNumber: string;
  machineId: number;
  machineSerialNumber: string;
  scheduledStart: string;
  scheduledEnd: string;
  status: AppointmentStatus;
}

export interface AppointmentRequest {
  patientId: number;
  staffId: number;
  roomId?: number;
  machineId?: number;
  scheduledStart: string;
  scheduledEnd: string;
}

export interface RescheduleAppointmentRequest {
  scheduledStart: string;
  scheduledEnd: string;
  roomId?: number;
  machineId?: number;
}

export interface AppointmentFilters {
  status?: string;
  staffId?: number;
  date?: string;
}
