import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
  FormField,
  email,
  form,
  maxLength,
  minLength,
  pattern,
  required,
  submit,
} from '@angular/forms/signals';
import { AuthStore } from '../../core/stores/auth.store';
import { SnackbarService } from '../../core/services/snackbar.service';

interface EditProfileFormModel {
  username: string;
  email: string;
  currentPassword: string;
  newPassword: string;
}

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    FormField,
  ],
})
export class EditProfileComponent {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly snackbar = inject(SnackbarService);

  protected readonly hideCurrentPassword = signal(true);
  protected readonly hideNewPassword = signal(true);

  protected goBack(): void {
    this.location.back();
  }

  protected toggleCurrentPasswordVisibility(): void {
    this.hideCurrentPassword.update((hidden) => !hidden);
  }

  protected toggleNewPasswordVisibility(): void {
    this.hideNewPassword.update((hidden) => !hidden);
  }

  protected readonly model = signal<EditProfileFormModel>({
    username: this.authStore.user()?.username ?? '',
    email: this.authStore.user()?.email ?? '',
    currentPassword: '',
    newPassword: '',
  });

  protected readonly editProfileForm = form(this.model, (schemaPath) => {
    required(schemaPath.username, { message: "Nom d'utilisateur requis" });
    minLength(schemaPath.username, 3, { message: 'Au moins 3 caractères' });
    maxLength(schemaPath.username, 20, { message: '20 caractères maximum' });
    pattern(schemaPath.username, /^\w+$/, {
      message: 'Uniquement des lettres, chiffres et underscores',
    });

    required(schemaPath.email, { message: 'Email requis' });
    email(schemaPath.email, { message: 'Adresse email invalide' });

    required(schemaPath.currentPassword, {
      message: 'Mot de passe actuel requis',
    });

    pattern(
      schemaPath.newPassword,
      /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$/,
      {
        message:
          'Au moins 8 caractères, avec une majuscule, une minuscule, un chiffre et un caractère spécial',
        when: (ctx) => ctx.value().length > 0,
      },
    );
  });

  protected onSubmit(event: Event): Promise<boolean> {
    event.preventDefault();
    return submit(this.editProfileForm, async (field) => {
      try {
        await firstValueFrom(this.authStore.updateProfile(field().value()));
        this.snackbar.success('Profil mis à jour.');
        this.router.navigate(['/profile']);
      } catch (error) {
        this.snackbar.showApiError(error);
      }
    });
  }
}
