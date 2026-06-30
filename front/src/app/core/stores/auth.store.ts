import { Service, computed, inject, signal } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { LoginRequest, RegisterRequest, UserResponse } from '../models';

const USER_STORAGE_KEY = 'mdd_user';

@Service()
export class AuthStore {
  readonly #authService = inject(AuthService);

  readonly #user = signal<UserResponse | null>(null);
  readonly user = this.#user.asReadonly();
  readonly isAuthenticated = computed(() => this.#user() !== null);

  #refresh$: Observable<void> | null = null;

  constructor() {
    const stored = localStorage.getItem(USER_STORAGE_KEY);
    if (stored) {
      try {
        this.#user.set(JSON.parse(stored) as UserResponse);
      } catch {
        localStorage.removeItem(USER_STORAGE_KEY);
      }
    }
  }

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.#authService.register(request).pipe(switchMap(() => this.loadUser()));
  }

  login(request: LoginRequest): Observable<UserResponse> {
    return this.#authService.login(request).pipe(switchMap(() => this.loadUser()));
  }

  loadUser(): Observable<UserResponse> {
    return this.#authService.getMe().pipe(tap((user) => this.setUser(user)));
  }

  refresh(): Observable<void> {
    if (this.#refresh$ !== null) {
      return this.#refresh$;
    }

    this.#refresh$ = this.#authService.refresh().pipe(
      finalize(() => (this.#refresh$ = null)),
      shareReplay(1),
    );

    return this.#refresh$;
  }

  restoreSession(): Observable<boolean> {
    if (this.#user()) {
      return of(true);
    }
    if (!this.#authService.accessToken) {
      return of(false);
    }
    return this.loadUser().pipe(
      map(() => true),
      catchError(() => {
        this.clearUser();
        return of(false);
      }),
    );
  }

  logout(): Observable<void> {
    return this.#authService.logout().pipe(tap(() => this.clearUser()));
  }

  private setUser(user: UserResponse): void {
    this.#user.set(user);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  }

  private clearUser(): void {
    this.#user.set(null);
    localStorage.removeItem(USER_STORAGE_KEY);
  }
}
