import { Role } from '../models/enums';

export function getDashboardPathForRole(role: Role): string {
  switch (role) {
    case Role.ADMIN:
      return '/dashboard/admin';
    case Role.DOCTOR:
      return '/dashboard/doctor';
    case Role.NURSE:
      return '/dashboard/nurse';
    case Role.PATIENT:
      return '/dashboard/patient';
    default:
      return '/login';
  }
}
