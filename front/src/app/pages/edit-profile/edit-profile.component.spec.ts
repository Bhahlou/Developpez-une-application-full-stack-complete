import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { EditProfileComponent } from './edit-profile.component';

describe('EditProfileComponent', () => {
  let component: EditProfileComponent;
  let fixture: ComponentFixture<EditProfileComponent>;
  let location: { back: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    location = { back: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [EditProfileComponent],
      providers: [{ provide: Location, useValue: location }],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProfileComponent);
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
});
