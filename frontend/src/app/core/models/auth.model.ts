import { Role } from './enums';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  id: number;
  name: string;
  email: string;
  role: Role;
  designation: string;
  accessToken: string;
}

export interface AuthenticatedUser {
  id: number;
  name: string;
  email: string;
  role: Role;
  designation: string;
}
