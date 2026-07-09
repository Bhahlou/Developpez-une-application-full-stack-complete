import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models';

const ACCESS_TOKEN_KEY = 'mdd_access_token';
const REFRESH_TOKEN_KEY = 'mdd_refresh_token';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/auth`;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('accessToken and refreshToken are null when nothing is stored', () => {
    expect(service.accessToken).toBeNull();
    expect(service.refreshToken).toBeNull();
  });

  it('register stores the returned tokens and completes with void', async () => {
    const promise = firstValueFrom(
      service.register({ username: 'johndoe', email: 'john@doe.com', password: 'Passw0rd!' }),
    );

    const req = httpTesting.expectOne(`${apiUrl}/register`);
    expect(req.request.method).toBe('POST');
    req.flush({ accessToken: 'access-token', refreshToken: 'refresh-token' } satisfies AuthResponse);

    await expect(promise).resolves.toBeUndefined();
    expect(service.accessToken).toBe('access-token');
    expect(service.refreshToken).toBe('refresh-token');
  });

  it('login stores the returned tokens and completes with void', async () => {
    const promise = firstValueFrom(service.login({ identifier: 'johndoe', password: 'Passw0rd!' }));

    const req = httpTesting.expectOne(`${apiUrl}/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ accessToken: 'access-token', refreshToken: 'refresh-token' } satisfies AuthResponse);

    await expect(promise).resolves.toBeUndefined();
    expect(service.accessToken).toBe('access-token');
    expect(service.refreshToken).toBe('refresh-token');
  });

  it('refresh throws without calling the API when no refresh token is stored', async () => {
    await expect(firstValueFrom(service.refresh())).rejects.toThrow('No refresh token available');
    httpTesting.expectNone(`${apiUrl}/refresh`);
  });

  it('refresh sends the stored refresh token and stores the new tokens', async () => {
    localStorage.setItem(REFRESH_TOKEN_KEY, 'old-refresh-token');

    const promise = firstValueFrom(service.refresh());

    const req = httpTesting.expectOne(`${apiUrl}/refresh`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ refreshToken: 'old-refresh-token' });
    req.flush({ accessToken: 'new-access-token', refreshToken: 'new-refresh-token' } satisfies AuthResponse);

    await expect(promise).resolves.toBeUndefined();
    expect(service.accessToken).toBe('new-access-token');
    expect(service.refreshToken).toBe('new-refresh-token');
  });

  it('logout clears tokens immediately and skips the API call when no refresh token is stored', async () => {
    localStorage.setItem(ACCESS_TOKEN_KEY, 'access-token');

    await expect(firstValueFrom(service.logout())).resolves.toBeUndefined();

    expect(service.accessToken).toBeNull();
    httpTesting.expectNone(`${apiUrl}/logout`);
  });

  it('logout clears tokens and calls the API when a refresh token is stored', async () => {
    localStorage.setItem(ACCESS_TOKEN_KEY, 'access-token');
    localStorage.setItem(REFRESH_TOKEN_KEY, 'refresh-token');

    const promise = firstValueFrom(service.logout());

    const req = httpTesting.expectOne(`${apiUrl}/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ refreshToken: 'refresh-token' });
    req.flush(null);

    await expect(promise).resolves.toBeUndefined();
    expect(service.accessToken).toBeNull();
    expect(service.refreshToken).toBeNull();
  });

  it('logout swallows API errors', async () => {
    localStorage.setItem(REFRESH_TOKEN_KEY, 'refresh-token');

    const promise = firstValueFrom(service.logout());

    const req = httpTesting.expectOne(`${apiUrl}/logout`);
    req.flush('error', { status: 500, statusText: 'Internal Server Error' });

    await expect(promise).resolves.toBeUndefined();
  });

  it('getMe issues a GET request and returns the user', async () => {
    const promise = firstValueFrom(service.getMe());

    const req = httpTesting.expectOne(`${apiUrl}/me`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, username: 'johndoe', email: 'john@doe.com' });

    await expect(promise).resolves.toEqual({ id: 1, username: 'johndoe', email: 'john@doe.com' });
  });

  it('updateMe stores the returned tokens and completes with void', async () => {
    const promise = firstValueFrom(
      service.updateMe({
        username: 'janedoe',
        email: 'jane@doe.com',
        currentPassword: 'Passw0rd!',
        newPassword: '',
      }),
    );

    const req = httpTesting.expectOne(`${apiUrl}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({
      username: 'janedoe',
      email: 'jane@doe.com',
      currentPassword: 'Passw0rd!',
      newPassword: '',
    });
    req.flush({ accessToken: 'new-access-token', refreshToken: 'new-refresh-token' } satisfies AuthResponse);

    await expect(promise).resolves.toBeUndefined();
    expect(service.accessToken).toBe('new-access-token');
    expect(service.refreshToken).toBe('new-refresh-token');
  });
});
