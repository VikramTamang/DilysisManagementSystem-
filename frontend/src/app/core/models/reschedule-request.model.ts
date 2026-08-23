export type RescheduleRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface RescheduleRequestResponse {
  id: number;
  appointmentId: number;
  patientId: number;
  patientName: string;
  staffId: number;
  staffName: string;
  originalStart: string;
  originalEnd: string;
  requestedStart: string;
  requestedEnd: string;
  reason: string;
  status: RescheduleRequestStatus;
  reviewNote: string | null;
  reviewedByUserId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface RescheduleRequestCreateRequest {
  appointmentId: number;
  requestedStart: string;
  requestedEnd: string;
  reason?: string;
}

export interface RescheduleRequestReviewRequest {
  reviewNote?: string;
}
