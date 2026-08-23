import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/enums';
import { getDashboardPathForRole } from '../utils/dashboard-route.util';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = route.data['roles'] as Role[] | undefined;
  const userRole = authService.userRole();

  if (!allowedRoles || !userRole) {
    router.navigate(['/login']);
    return false;
  }

  if (allowedRoles.includes(userRole)) {
    return true;
  }

  // Logged in, but this isn't their dashboard — send them to their real one
  router.navigate([getDashboardPathForRole(userRole)]);
  return false;
};
