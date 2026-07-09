import { beforeEach, describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SnackbarService } from './snackbar.service';

describe('SnackbarService', () => {
  let service: SnackbarService;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    snackBar = { open: vi.fn() };

    TestBed.configureTestingModule({
      providers: [{ provide: MatSnackBar, useValue: snackBar }],
    });

    service = TestBed.inject(SnackbarService);
  });

  it('success opens a snackbar with the success panel class', () => {
    service.success('Saved!');

    expect(snackBar.open).toHaveBeenCalledWith('Saved!', 'Fermer', {
      duration: 3000,
      panelClass: 'snackbar-success',
    });
  });

  it('error opens a snackbar with the error panel class', () => {
    service.error('Oops!');

    expect(snackBar.open).toHaveBeenCalledWith('Oops!', 'Fermer', {
      duration: 5000,
      panelClass: 'snackbar-error',
    });
  });

  it('showApiError resolves the message from a known error code', () => {
    service.showApiError({ error: { code: 'AUTH_BAD_CREDENTIALS', message: 'raw message' } });

    expect(snackBar.open).toHaveBeenCalledWith('Identifiant ou mot de passe incorrect.', 'Fermer', {
      duration: 5000,
      panelClass: 'snackbar-error',
    });
  });

  it('showApiError falls back to the raw message when the code is unknown', () => {
    service.showApiError({ error: { code: 'SOMETHING_ELSE', message: 'custom message' } });

    expect(snackBar.open).toHaveBeenCalledWith('custom message', 'Fermer', {
      duration: 5000,
      panelClass: 'snackbar-error',
    });
  });

  it('showApiError falls back to the default message when the error has no usable body', () => {
    service.showApiError(new Error('network down'));

    expect(snackBar.open).toHaveBeenCalledWith('Une erreur est survenue, réessayez.', 'Fermer', {
      duration: 5000,
      panelClass: 'snackbar-error',
    });
  });
});
