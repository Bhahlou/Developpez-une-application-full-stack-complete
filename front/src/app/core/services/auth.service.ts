import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
  UserResponse,
} from '../models';

const ACCESS_TOKEN_KEY = 'mdd_access_token';
const REFRESH_TOKEN_KEY = 'mdd_refresh_token';

@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  get accessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, request)
      .pipe(tap((auth) => this.storeTokens(auth)), map(() => undefined));
  }

  login(request: LoginRequest): Observable<void> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(tap((auth) => this.storeTokens(auth)), map(() => undefined));
  }

  refresh(): Observable<void> {
    const refreshToken = this.refreshToken;
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http
      .post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(tap((auth) => this.storeTokens(auth)), map(() => undefined));
  }

  logout(): Observable<void> {
    const refreshToken = this.refreshToken;
    this.clearTokens();

    if (!refreshToken) {
      return of(undefined);
    }

    return this.http.post(`${this.apiUrl}/logout`, { refreshToken }).pipe(
      map(() => undefined),
      catchError(() => of(undefined)),
    );
  }

  getMe(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

  updateMe(request: UpdateProfileRequest): Observable<void> {
    return this.http
      .put<AuthResponse>(`${this.apiUrl}/me`, request)
      .pipe(tap((auth) => this.storeTokens(auth)), map(() => undefined));
  }

  private storeTokens(auth: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
  }

  private clearTokens(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}
