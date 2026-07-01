import { beforeEach, describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Subject, firstValueFrom, of, throwError } from 'rxjs';
import { AuthStore } from './auth.store';
import { AuthService } from '../services/auth.service';
import { UserResponse } from '../models';

const USER_STORAGE_KEY = 'mdd_user';

interface MockAuthService {
  register: ReturnType<typeof vi.fn>;
  login: ReturnType<typeof vi.fn>;
  getMe: ReturnType<typeof vi.fn>;
  refresh: ReturnType<typeof vi.fn>;
  logout: ReturnType<typeof vi.fn>;
  accessToken: string | null;
}

describe('AuthStore', () => {
  let store: AuthStore;
  let authService: MockAuthService;

  const user: UserResponse = { id: 1, username: 'johndoe', email: 'john@doe.com' };

  function configure(): void {
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authService }],
    });
    store = TestBed.inject(AuthStore);
  }

  beforeEach(() => {
    localStorage.clear();
    authService = {
      register: vi.fn(),
      login: vi.fn(),
      getMe: vi.fn(),
      refresh: vi.fn(),
      logout: vi.fn(),
      accessToken: null,
    };
  });

  it('starts with no user when localStorage is empty', () => {
    configure();

    expect(store.user()).toBeNull();
    expect(store.isAuthenticated()).toBe(false);
  });

  it('restores the user from localStorage on creation', () => {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));

    configure();

    expect(store.user()).toEqual(user);
    expect(store.isAuthenticated()).toBe(true);
  });

  it('clears corrupted localStorage data on creation', () => {
    localStorage.setItem(USER_STORAGE_KEY, 'not-json');

    configure();

    expect(store.user()).toBeNull();
    expect(localStorage.getItem(USER_STORAGE_KEY)).toBeNull();
  });

  it('register loads and stores the user after registering', async () => {
    configure();
    authService.register.mockReturnValue(of(undefined));
    authService.getMe.mockReturnValue(of(user));

    const result = await firstValueFrom(
      store.register({ username: 'johndoe', email: 'john@doe.com', password: 'Passw0rd!' }),
    );

    expect(result).toEqual(user);
    expect(store.user()).toEqual(user);
    expect(localStorage.getItem(USER_STORAGE_KEY)).toBe(JSON.stringify(user));
  });

  it('login loads and stores the user after logging in', async () => {
    configure();
    authService.login.mockReturnValue(of(undefined));
    authService.getMe.mockReturnValue(of(user));

    const result = await firstValueFrom(store.login({ identifier: 'johndoe', password: 'Passw0rd!' }));

    expect(result).toEqual(user);
    expect(store.user()).toEqual(user);
  });

  it('refresh deduplicates calls made before the previous one completes', () => {
    configure();
    const pending = new Subject<void>();
    authService.refresh.mockReturnValue(pending.asObservable());

    store.refresh().subscribe();
    store.refresh().subscribe();

    expect(authService.refresh).toHaveBeenCalledTimes(1);

    pending.next();
    pending.complete();
  });

  it('refresh issues a new call once the previous one has completed', () => {
    configure();
    const pending = new Subject<void>();
    authService.refresh.mockReturnValueOnce(pending.asObservable());

    store.refresh().subscribe();
    pending.next();
    pending.complete();

    authService.refresh.mockReturnValueOnce(of(undefined));
    store.refresh().subscribe();

    expect(authService.refresh).toHaveBeenCalledTimes(2);
  });

  it('restoreSession resolves true without calling the API when a user is already loaded', async () => {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    configure();

    const result = await firstValueFrom(store.restoreSession());

    expect(result).toBe(true);
    expect(authService.getMe).not.toHaveBeenCalled();
  });

  it('restoreSession resolves false without calling the API when there is no access token', async () => {
    configure();
    authService.accessToken = null;

    const result = await firstValueFrom(store.restoreSession());

    expect(result).toBe(false);
    expect(authService.getMe).not.toHaveBeenCalled();
  });

  it('restoreSession loads the user and resolves true when an access token is stored', async () => {
    configure();
    authService.accessToken = 'access-token';
    authService.getMe.mockReturnValue(of(user));

    const result = await firstValueFrom(store.restoreSession());

    expect(result).toBe(true);
    expect(store.user()).toEqual(user);
  });

  it('restoreSession clears the user and resolves false when loading the user fails', async () => {
    configure();
    authService.accessToken = 'access-token';
    authService.getMe.mockReturnValue(throwError(() => new Error('unauthorized')));

    const result = await firstValueFrom(store.restoreSession());

    expect(result).toBe(false);
    expect(store.user()).toBeNull();
  });

  it('logout clears the user', async () => {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    configure();
    authService.logout.mockReturnValue(of(undefined));

    await firstValueFrom(store.logout());

    expect(store.user()).toBeNull();
    expect(localStorage.getItem(USER_STORAGE_KEY)).toBeNull();
  });
});
