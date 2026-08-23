import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export interface NormalizedError {
  message: string;
  errorCode: string | null;
  status: number;
  fieldErrors?: Record<string, string>;
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const body = err.error;

      const normalized: NormalizedError = {
        message: body?.message ?? 'Something went wrong. Please try again.',
        errorCode: body?.errorCode ?? null,
        status: err.status,
        fieldErrors: body?.errorCode === 'VALIDATION_ERROR' ? body?.data : undefined,
      };

      return throwError(() => normalized);
    }),
  );
};
