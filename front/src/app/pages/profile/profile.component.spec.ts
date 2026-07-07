import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { ProfileComponent } from './profile.component';
import { AuthStore } from '../../core/stores/auth.store';
import { SubscriptionService } from '../../core/services/subscription.service';
import { SnackbarService } from '../../core/services/snackbar.service';
import { UserResponse } from '../../core/models';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let authStore: { user: ReturnType<typeof signal<UserResponse | null>> };
  let location: { back: ReturnType<typeof vi.fn> };
  let subscriptionService: { getMySubscriptions: ReturnType<typeof vi.fn>; unsubscribe: ReturnType<typeof vi.fn> };
  let snackbar: { success: ReturnType<typeof vi.fn>; showApiError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authStore = { user: signal<UserResponse | null>({ id: 1, username: 'johndoe', email: 'john@doe.com' }) };
    location = { back: vi.fn() };
    subscriptionService = {
      getMySubscriptions: vi.fn().mockReturnValue(of([])),
      unsubscribe: vi.fn(),
    };
    snackbar = { success: vi.fn(), showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        provideRouter([]),
        { provide: AuthStore, useValue: authStore },
        { provide: Location, useValue: location },
        { provide: SubscriptionService, useValue: subscriptionService },
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('displays the authenticated username and email', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('johndoe');
    expect(text).toContain('john@doe.com');
  });

  it('goes back in history', () => {
    component['goBack']();

    expect(location.back).toHaveBeenCalled();
  });

  it('loads the subscriptions on init', async () => {
    const subscriptions = [{ id: 1, title: 'Backend', description: 'desc', subscribed: true }];
    subscriptionService.getMySubscriptions.mockReturnValue(of(subscriptions));

    await component['loadSubscriptions']();

    expect(component['subscriptions']()).toEqual(subscriptions);
    expect(component['loadingSubscriptions']()).toBe(false);
  });

  it('shows an API error when loading subscriptions fails', async () => {
    const error = new Error('failed');
    subscriptionService.getMySubscriptions.mockReturnValue(throwError(() => error));

    await component['loadSubscriptions']();

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(component['loadingSubscriptions']()).toBe(false);
  });

  it('unsubscribes from a theme and removes it from the list', async () => {
    component['subscriptions'].set([{ id: 1, title: 'Backend', description: 'desc', subscribed: true }]);
    subscriptionService.unsubscribe.mockReturnValue(of(undefined));

    await component['unsubscribe']({ id: 1, title: 'Backend', description: 'desc', subscribed: true });

    expect(subscriptionService.unsubscribe).toHaveBeenCalledWith(1);
    expect(component['subscriptions']()).toEqual([]);
    expect(snackbar.success).toHaveBeenCalledWith('Désabonnement effectué.');
  });

  it('shows an API error when unsubscribing fails', async () => {
    const theme = { id: 1, title: 'Backend', description: 'desc', subscribed: true };
    component['subscriptions'].set([theme]);
    const error = new Error('failed');
    subscriptionService.unsubscribe.mockReturnValue(throwError(() => error));

    await component['unsubscribe'](theme);

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(component['subscriptions']()).toEqual([theme]);
  });
});
