import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { ThemeService } from '../../../core/services/theme.service';
import { ThemeResponse } from '../../../core/models';
import { SnackbarService } from '../../../core/services/snackbar.service';

interface CreateThemeFormModel {
  title: string;
  description: string;
}

@Component({
  selector: 'app-create-theme-dialog',
  templateUrl: './create-theme-dialog.component.html',
  styleUrls: ['./create-theme-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, FormField],
})
export class CreateThemeDialogComponent {
  private readonly themeService = inject(ThemeService);
  private readonly snackbar = inject(SnackbarService);
  private readonly dialogRef = inject(MatDialogRef<CreateThemeDialogComponent, ThemeResponse>);

  protected readonly model = signal<CreateThemeFormModel>({ title: '', description: '' });

  protected readonly createThemeForm = form(this.model, (schemaPath) => {
    required(schemaPath.title, { message: 'Titre requis' });
    maxLength(schemaPath.title, 100, { message: '100 caractères maximum' });

    required(schemaPath.description, { message: 'Description requise' });
    maxLength(schemaPath.description, 1000, { message: '1000 caractères maximum' });
  });

  protected cancel(): void {
    this.dialogRef.close();
  }

  protected onSubmit(event: Event): Promise<boolean> {
    event.preventDefault();
    return submit(this.createThemeForm, async (field) => {
      try {
        const theme = await firstValueFrom(this.themeService.create(field().value()));
        this.dialogRef.close(theme);
      } catch (error) {
        this.snackbar.showApiError(error);
      }
    });
  }
}
