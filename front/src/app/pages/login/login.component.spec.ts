import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthStore } from '../../core/stores/auth.store';
import { SnackbarService } from '../../core/services/snackbar.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authStore: { login: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let location: { back: ReturnType<typeof vi.fn> };
  let snackbar: { showApiError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authStore = { login: vi.fn() };
    router = { navigate: vi.fn() };
    location = { back: vi.fn() };
    snackbar = { showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthStore, useValue: authStore },
        { provide: Router, useValue: router },
        { provide: Location, useValue: location },
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('goes back in history', () => {
    component['goBack']();

    expect(location.back).toHaveBeenCalled();
  });

  it('toggles password visibility', () => {
    expect(component['hidePassword']()).toBe(true);

    component['togglePasswordVisibility']();

    expect(component['hidePassword']()).toBe(false);
  });

  it('does not call the API when the form is invalid', async () => {
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(authStore.login).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('logs in and navigates to the feed on success', async () => {
    component['model'].set({ identifier: 'johndoe', password: 'Passw0rd!' });
    authStore.login.mockReturnValue(of({ id: 1, username: 'johndoe', email: 'john@doe.com' }));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(authStore.login).toHaveBeenCalledWith({ identifier: 'johndoe', password: 'Passw0rd!' });
    expect(router.navigate).toHaveBeenCalledWith(['/feed']);
  });

  it('shows an API error and does not navigate on failure', async () => {
    component['model'].set({ identifier: 'johndoe', password: 'Passw0rd!' });
    const error = new Error('bad credentials');
    authStore.login.mockReturnValue(throwError(() => error));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
