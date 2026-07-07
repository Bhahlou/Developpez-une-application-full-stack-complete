import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { AuthStore } from '../../core/stores/auth.store';
import { SnackbarService } from '../../core/services/snackbar.service';

interface LoginFormModel {
  identifier: string;
  password: string;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, FormField],
})
export class LoginComponent {
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

  protected readonly model = signal<LoginFormModel>({ identifier: '', password: '' });
  protected readonly loginForm = form(this.model, (schemaPath) => {
    required(schemaPath.identifier, { message: "Identifiant requis" });
    required(schemaPath.password, { message: 'Mot de passe requis' });
  });

  protected onSubmit(event: Event): Promise<boolean> {
    event.preventDefault();
    return submit(this.loginForm, async (field) => {
      try {
        await firstValueFrom(this.authStore.login(field().value()));
        this.router.navigate(['/feed']);
      } catch (error) {
        this.snackbar.showApiError(error);
      }
    });
  }
}
