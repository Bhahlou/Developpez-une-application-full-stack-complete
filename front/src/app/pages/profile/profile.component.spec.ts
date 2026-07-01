import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { ProfileComponent } from './profile.component';
import { AuthStore } from '../../core/stores/auth.store';
import { UserResponse } from '../../core/models';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let authStore: { user: ReturnType<typeof signal<UserResponse | null>> };
  let location: { back: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authStore = { user: signal<UserResponse | null>({ id: 1, username: 'johndoe', email: 'john@doe.com' }) };
    location = { back: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        provideRouter([]),
        { provide: AuthStore, useValue: authStore },
        { provide: Location, useValue: location },
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
});
