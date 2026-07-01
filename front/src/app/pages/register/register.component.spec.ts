import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RegisterComponent } from './register.component';
import { AuthStore } from '../../core/stores/auth.store';
import { SnackbarService } from '../../core/services/snackbar.service';

function flushMicrotasks(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authStore: { register: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let location: { back: ReturnType<typeof vi.fn> };
  let snackbar: { showApiError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authStore = { register: vi.fn() };
    router = { navigate: vi.fn() };
    location = { back: vi.fn() };
    snackbar = { showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: AuthStore, useValue: authStore },
        { provide: Router, useValue: router },
        { provide: Location, useValue: location },
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
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

    component['onSubmit'](event);
    await flushMicrotasks();

    expect(authStore.register).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('registers and navigates to the dashboard on success', async () => {
    component['model'].set({ username: 'johndoe', email: 'john@doe.com', password: 'Passw0rd!' });
    authStore.register.mockReturnValue(of({ id: 1, username: 'johndoe', email: 'john@doe.com' }));
    const event = new Event('submit');

    component['onSubmit'](event);
    await flushMicrotasks();

    expect(authStore.register).toHaveBeenCalledWith({
      username: 'johndoe',
      email: 'john@doe.com',
      password: 'Passw0rd!',
    });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('shows an API error and does not navigate on failure', async () => {
    component['model'].set({ username: 'johndoe', email: 'john@doe.com', password: 'Passw0rd!' });
    const error = new Error('username taken');
    authStore.register.mockReturnValue(throwError(() => error));
    const event = new Event('submit');

    component['onSubmit'](event);
    await flushMicrotasks();

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
