export interface StaffUnavailabilityRequest {
  staffId: number;
  unavailableStart: string;
  unavailableEnd: string;
  reason?: string;
}

export interface ReassignmentResultResponse {
  appointmentId: number;
  outcome: 'REASSIGNED' | 'PENDING_REASSIGNMENT' | string;
  previousStaffId: number;
  previousStaffName: string;
  newStaffId: number | null;
  newStaffName: string | null;
  scheduledStart: string;
  scheduledEnd: string;
  note: string;
}

export interface EmergencyReassignmentResponse {
  staffId: number;
  staffName: string;
  unavailableStart: string;
  unavailableEnd: string;
  reason: string | null;
  totalAffected: number;
  reassignedCount: number;
  pendingCount: number;
  results: ReassignmentResultResponse[];
}
