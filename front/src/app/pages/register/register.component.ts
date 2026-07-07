import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
} from '@angular/core';
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

interface RegisterFormModel {
  username: string;
  email: string;
  password: string;
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    FormField,
  ],
})
export class RegisterComponent {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly snackbar = inject(SnackbarService);

  protected readonly hidePassword = signal(true);

  protected goBack(): void {
    this.location.back();
  }

  protected togglePasswordVisibility(): void {
    this.hidePassword.update((hidden) => !hidden);
  }

  protected readonly model = signal<RegisterFormModel>({
    username: '',
    email: '',
    password: '',
  });
  protected readonly registerForm = form(this.model, (schemaPath) => {
    required(schemaPath.username, { message: "Nom d'utilisateur requis" });
    minLength(schemaPath.username, 3, { message: 'Au moins 3 caractères' });
    maxLength(schemaPath.username, 20, { message: '20 caractères maximum' });
    pattern(schemaPath.username, /^\w+$/, {
      message: 'Uniquement des lettres, chiffres et underscores',
    });

    required(schemaPath.email, { message: 'Email requis' });
    email(schemaPath.email, { message: 'Adresse email invalide' });

    required(schemaPath.password, { message: 'Mot de passe requis' });
    pattern(
      schemaPath.password,
      /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$/,
      {
        message:
          'Au moins 8 caractères, avec une majuscule, une minuscule, un chiffre et un caractère spécial',
      },
    );
  });

  protected onSubmit(event: Event): Promise<boolean> {
    event.preventDefault();
    return submit(this.registerForm, async (field) => {
      try {
        await firstValueFrom(this.authStore.register(field().value()));
        this.router.navigate(['/feed']);
      } catch (error) {
        this.snackbar.showApiError(error);
      }
    });
  }
}
