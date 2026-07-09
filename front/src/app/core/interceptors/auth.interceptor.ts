import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { AuthStore } from '../stores/auth.store';

const AUTH_ENDPOINTS = ['/auth/login', '/auth/register', '/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const authStore = inject(AuthStore);
  const router = inject(Router);

  const accessToken = authService.accessToken;
  const authReq = accessToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthEndpoint = AUTH_ENDPOINTS.some((endpoint) => req.url.includes(endpoint));
      if (error.status !== 401 || isAuthEndpoint) {
        return throwError(() => error);
      }

      return authStore.refresh().pipe(
        switchMap(() => {
          const retryToken = authService.accessToken;
          const retryReq = retryToken
            ? req.clone({ setHeaders: { Authorization: `Bearer ${retryToken}` } })
            : req;
          return next(retryReq);
        }),
        catchError((refreshError) => {
          authStore.logout().subscribe();
          router.navigate(['/login']);
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
