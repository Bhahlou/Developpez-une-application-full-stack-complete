import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { firstValueFrom, of, throwError } from 'rxjs';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { AuthStore } from '../stores/auth.store';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let authService: { accessToken: string | null };
  let authStore: { refresh: ReturnType<typeof vi.fn>; logout: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = { accessToken: null };
    authStore = { refresh: vi.fn(), logout: vi.fn().mockReturnValue(of(undefined)) };
    router = { navigate: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
        { provide: AuthStore, useValue: authStore },
        { provide: Router, useValue: router },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('adds an Authorization header when an access token is stored', () => {
    authService.accessToken = 'access-token';

    httpClient.get('/api/dashboard').subscribe();

    const req = httpTesting.expectOne('/api/dashboard');
    expect(req.request.headers.get('Authorization')).toBe('Bearer access-token');
    req.flush({});
  });

  it('does not add an Authorization header when no access token is stored', () => {
    httpClient.get('/api/dashboard').subscribe();

    const req = httpTesting.expectOne('/api/dashboard');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('propagates non-401 errors without attempting a refresh', async () => {
    const promise = firstValueFrom(httpClient.get('/api/dashboard'));

    const req = httpTesting.expectOne('/api/dashboard');
    req.flush('error', { status: 500, statusText: 'Internal Server Error' });

    await expect(promise).rejects.toMatchObject({ status: 500 });
    expect(authStore.refresh).not.toHaveBeenCalled();
  });

  it('propagates 401 errors from auth endpoints without attempting a refresh', async () => {
    const promise = firstValueFrom(httpClient.post('/api/auth/login', {}));

    const req = httpTesting.expectOne('/api/auth/login');
    req.flush('error', { status: 401, statusText: 'Unauthorized' });

    await expect(promise).rejects.toMatchObject({ status: 401 });
    expect(authStore.refresh).not.toHaveBeenCalled();
  });

  it('refreshes the token and retries the request on a 401 from a non-auth endpoint', async () => {
    authService.accessToken = 'expired-token';
    authStore.refresh.mockImplementation(() => {
      authService.accessToken = 'new-token';
      return of(undefined);
    });

    const promise = firstValueFrom(httpClient.get('/api/dashboard'));

    const firstReq = httpTesting.expectOne('/api/dashboard');
    expect(firstReq.request.headers.get('Authorization')).toBe('Bearer expired-token');
    firstReq.flush('error', { status: 401, statusText: 'Unauthorized' });

    const retryReq = httpTesting.expectOne('/api/dashboard');
    expect(retryReq.request.headers.get('Authorization')).toBe('Bearer new-token');
    retryReq.flush({ ok: true });

    await expect(promise).resolves.toEqual({ ok: true });
  });

  it('retries without an Authorization header when the refresh does not yield a new token', async () => {
    authService.accessToken = 'expired-token';
    authStore.refresh.mockImplementation(() => {
      authService.accessToken = null;
      return of(undefined);
    });

    const promise = firstValueFrom(httpClient.get('/api/dashboard'));

    const firstReq = httpTesting.expectOne('/api/dashboard');
    firstReq.flush('error', { status: 401, statusText: 'Unauthorized' });

    const retryReq = httpTesting.expectOne('/api/dashboard');
    expect(retryReq.request.headers.has('Authorization')).toBe(false);
    retryReq.flush({ ok: true });

    await expect(promise).resolves.toEqual({ ok: true });
  });

  it('logs out and redirects to /login when the refresh itself fails', async () => {
    authStore.refresh.mockReturnValue(throwError(() => new Error('refresh failed')));

    const promise = firstValueFrom(httpClient.get('/api/dashboard'));

    const req = httpTesting.expectOne('/api/dashboard');
    req.flush('error', { status: 401, statusText: 'Unauthorized' });

    await expect(promise).rejects.toThrow('refresh failed');
    expect(authStore.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
