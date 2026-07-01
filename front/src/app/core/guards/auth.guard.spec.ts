import { beforeEach, describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, firstValueFrom, of } from 'rxjs';
import { authGuard, guestGuard } from './auth.guard';
import { AuthStore } from '../stores/auth.store';

function runGuard(guard: CanActivateFn): Observable<boolean | UrlTree> {
  return TestBed.runInInjectionContext(() =>
    guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
  ) as Observable<boolean | UrlTree>;
}

describe('auth guards', () => {
  let authStore: { restoreSession: ReturnType<typeof vi.fn> };
  let router: { createUrlTree: ReturnType<typeof vi.fn> };
  let urlTree: UrlTree;

  beforeEach(() => {
    urlTree = {} as UrlTree;
    authStore = { restoreSession: vi.fn() };
    router = { createUrlTree: vi.fn().mockReturnValue(urlTree) };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthStore, useValue: authStore },
        { provide: Router, useValue: router },
      ],
    });
  });

  describe('authGuard', () => {
    it('allows navigation when authenticated', async () => {
      authStore.restoreSession.mockReturnValue(of(true));

      const result = await firstValueFrom(runGuard(authGuard));

      expect(result).toBe(true);
    });

    it('redirects to /login when not authenticated', async () => {
      authStore.restoreSession.mockReturnValue(of(false));

      const result = await firstValueFrom(runGuard(authGuard));

      expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
      expect(result).toBe(urlTree);
    });
  });

  describe('guestGuard', () => {
    it('allows navigation when not authenticated', async () => {
      authStore.restoreSession.mockReturnValue(of(false));

      const result = await firstValueFrom(runGuard(guestGuard));

      expect(result).toBe(true);
    });

    it('redirects to /dashboard when already authenticated', async () => {
      authStore.restoreSession.mockReturnValue(of(true));

      const result = await firstValueFrom(runGuard(guestGuard));

      expect(router.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
      expect(result).toBe(urlTree);
    });
  });
});
