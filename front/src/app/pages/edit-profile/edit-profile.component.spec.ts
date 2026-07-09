import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EditProfileComponent } from './edit-profile.component';
import { AuthStore } from '../../core/stores/auth.store';
import { SnackbarService } from '../../core/services/snackbar.service';

describe('EditProfileComponent', () => {
  let component: EditProfileComponent;
  let fixture: ComponentFixture<EditProfileComponent>;
  let authStore: { user: ReturnType<typeof vi.fn>; updateProfile: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let location: { back: ReturnType<typeof vi.fn> };
  let snackbar: { success: ReturnType<typeof vi.fn>; showApiError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authStore = {
      user: vi.fn().mockReturnValue({ id: 1, username: 'johndoe', email: 'john@doe.com' }),
      updateProfile: vi.fn(),
    };
    router = { navigate: vi.fn() };
    location = { back: vi.fn() };
    snackbar = { success: vi.fn(), showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [EditProfileComponent],
      providers: [
        { provide: AuthStore, useValue: authStore },
        { provide: Router, useValue: router },
        { provide: Location, useValue: location },
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('pre-fills the form with the current user', () => {
    expect(component['model']()).toEqual({
      username: 'johndoe',
      email: 'john@doe.com',
      currentPassword: '',
      newPassword: '',
    });
  });

  it('pre-fills the form with empty strings when there is no current user', () => {
    authStore.user.mockReturnValue(null);

    const otherFixture = TestBed.createComponent(EditProfileComponent);

    expect(otherFixture.componentInstance['model']()).toEqual({
      username: '',
      email: '',
      currentPassword: '',
      newPassword: '',
    });
  });

  it('goes back in history', () => {
    component['goBack']();

    expect(location.back).toHaveBeenCalled();
  });

  it('toggles current password visibility', () => {
    expect(component['hideCurrentPassword']()).toBe(true);

    component['toggleCurrentPasswordVisibility']();

    expect(component['hideCurrentPassword']()).toBe(false);
  });

  it('toggles new password visibility', () => {
    expect(component['hideNewPassword']()).toBe(true);

    component['toggleNewPasswordVisibility']();

    expect(component['hideNewPassword']()).toBe(false);
  });

  it('does not call the API when the form is invalid', async () => {
    component['model'].update((model) => ({ ...model, currentPassword: '' }));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(authStore.updateProfile).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('updates the profile and navigates to the profile page on success', async () => {
    component['model'].set({
      username: 'janedoe',
      email: 'jane@doe.com',
      currentPassword: 'Passw0rd!',
      newPassword: '',
    });
    authStore.updateProfile.mockReturnValue(of({ id: 1, username: 'janedoe', email: 'jane@doe.com' }));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(authStore.updateProfile).toHaveBeenCalledWith({
      username: 'janedoe',
      email: 'jane@doe.com',
      currentPassword: 'Passw0rd!',
      newPassword: '',
    });
    expect(snackbar.success).toHaveBeenCalledWith('Profil mis à jour.');
    expect(router.navigate).toHaveBeenCalledWith(['/profile']);
  });

  it('shows an API error and does not navigate on failure', async () => {
    component['model'].set({
      username: 'janedoe',
      email: 'jane@doe.com',
      currentPassword: 'wrong-password',
      newPassword: '',
    });
    const error = new Error('bad credentials');
    authStore.updateProfile.mockReturnValue(throwError(() => error));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
