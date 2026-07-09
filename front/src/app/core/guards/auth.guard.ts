import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthStore } from '../stores/auth.store';

export const authGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const router = inject(Router);

  return authStore
    .restoreSession()
    .pipe(map((isAuthenticated) => isAuthenticated || router.createUrlTree(['/login'])));
};

export const guestGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const router = inject(Router);

  return authStore
    .restoreSession()
    .pipe(map((isAuthenticated) => (isAuthenticated ? router.createUrlTree(['/feed']) : true)));
};
