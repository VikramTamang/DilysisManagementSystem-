export interface RoomUtilizationResponse {
  roomId: number;
  roomNumber: string;
  totalAppointments: number;
  utilizedMinutes: number;
  utilizationPercentage: number;
}

export interface MachineUtilizationResponse {
  machineId: number;
  serialNumber: string;
  totalAppointments: number;
  utilizedMinutes: number;
  utilizationPercentage: number;
}

export interface UtilizationReportResponse {
  startWindow: string;
  endWindow: string;
  totalAppointments: number;
  roomUtilization: RoomUtilizationResponse[];
  machineUtilization: MachineUtilizationResponse[];
}

export interface StaffActivityResponse {
  staffId: number;
  staffName: string;
  staffRole: string;
  totalScheduled: number;
  totalCompleted: number;
  totalCancelled: number;
  totalRescheduled: number;
  totalReassignments: number;
}

export interface AuditLogResponse {
  id: number;
  appointmentId: number;
  action: string;
  performedByUserId: number | null;
  performedByRole: string | null;
  oldStatus: string | null;
  newStatus: string | null;
  oldScheduledStart: string | null;
  newScheduledStart: string | null;
  oldScheduledEnd: string | null;
  newScheduledEnd: string | null;
  oldRoomId: number | null;
  newRoomId: number | null;
  oldMachineId: number | null;
  newMachineId: number | null;
  oldStaffId: number | null;
  newStaffId: number | null;
  createdAt: string;
}
