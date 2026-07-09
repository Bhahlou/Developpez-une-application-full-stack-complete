import { Service, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiErrorResponse } from '../models';
import { DEFAULT_ERROR_MESSAGE, ERROR_MESSAGES } from '../constants/error-messages';

@Service()
export class SnackbarService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 3000, panelClass: 'snackbar-success' });
  }

  error(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 5000, panelClass: 'snackbar-error' });
  }

  showApiError(error: unknown): void {
    this.error(this.resolveErrorMessage(error));
  }

  private resolveErrorMessage(error: unknown): string {
    const body = this.extractErrorBody(error);
    if (body?.code && ERROR_MESSAGES[body.code]) {
      return ERROR_MESSAGES[body.code];
    }
    if (body?.message) {
      return body.message;
    }
    return DEFAULT_ERROR_MESSAGE;
  }

  private extractErrorBody(error: unknown): Partial<ApiErrorResponse> | undefined {
    if (error && typeof error === 'object' && 'error' in error) {
      return (error as { error?: Partial<ApiErrorResponse> }).error;
    }
    return undefined;
  }
}
