export type StaffType = 'DOCTOR' | 'NURSE';

export interface DoctorRequest {
  name: string;
  email: string;
  password?: string; // required on create, omit/blank on update to keep existing password
  phone?: string;
  licenseNumber?: string;
  specialization?: string;
  consultationFee?: number;
  experienceYears?: number;
}

export interface DoctorResponse {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  licenseNumber: string | null;
  specialization: string | null;
  consultationFee: number | null;
  experienceYears: number | null;
  accountStatus: string;
}

export interface NurseRequest {
  name: string;
  email: string;
  password?: string;
  phone?: string;
  qualification?: string;
  shift?: string;
  assignedDepartment?: string;
  experienceYears?: number;
}

export interface NurseResponse {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  qualification: string | null;
  shift: string | null;
  assignedDepartment: string | null;
  experienceYears: number | null;
  accountStatus: string;
}

export interface StaffStatusRequest {
  accountStatus: 'ACTIVE' | 'SUSPENDED';
}

export interface StaffMember {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  experienceYears: number | null;
  accountStatus: string;
  staffType: StaffType;
  raw: DoctorResponse | NurseResponse;
}
