import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CreateThemeDialogComponent } from './create-theme-dialog.component';
import { ThemeService } from '../../../core/services/theme.service';
import { SnackbarService } from '../../../core/services/snackbar.service';
import { MatDialogRef } from '@angular/material/dialog';

describe('CreateThemeDialogComponent', () => {
  let component: CreateThemeDialogComponent;
  let fixture: ComponentFixture<CreateThemeDialogComponent>;
  let themeService: { create: ReturnType<typeof vi.fn> };
  let snackbar: { showApiError: ReturnType<typeof vi.fn> };
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    themeService = { create: vi.fn() };
    snackbar = { showApiError: vi.fn() };
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [CreateThemeDialogComponent],
      providers: [
        { provide: ThemeService, useValue: themeService },
        { provide: SnackbarService, useValue: snackbar },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateThemeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('closes the dialog without a result when cancelled', () => {
    component['cancel']();

    expect(dialogRef.close).toHaveBeenCalledWith();
  });

  it('does not call the API when the form is invalid', async () => {
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(themeService.create).not.toHaveBeenCalled();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('creates the theme and closes the dialog with the result on success', async () => {
    component['model'].set({ title: 'Backend', description: 'desc' });
    const created = { id: 1, title: 'Backend', description: 'desc', subscribed: false };
    themeService.create.mockReturnValue(of(created));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(themeService.create).toHaveBeenCalledWith({ title: 'Backend', description: 'desc' });
    expect(dialogRef.close).toHaveBeenCalledWith(created);
  });

  it('shows an API error and does not close the dialog on failure', async () => {
    component['model'].set({ title: 'Backend', description: 'desc' });
    const error = new Error('conflict');
    themeService.create.mockReturnValue(throwError(() => error));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(dialogRef.close).not.toHaveBeenCalled();
  });
});
