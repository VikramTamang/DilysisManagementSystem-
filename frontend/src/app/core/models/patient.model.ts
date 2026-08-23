export interface PatientRegistrationRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
  address?: string;
  dateOfBirth?: string; // ISO format YYYY-MM-DD, matches backend LocalDate
  bloodGroup?: string;
}

export interface PatientRegistrationResponse {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  address: string | null;
  dateOfBirth: string | null;
  bloodGroup: string | null;
  assignedDoctorId: number | null;
  dialysisHistory: string | null;
  treatmentNotes: string | null;
  totalSessions: number;
  accountStatus: string;
}
