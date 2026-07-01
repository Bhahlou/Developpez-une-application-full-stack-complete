import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { Location } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthStore } from '../../core/stores/auth.store';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [RouterLink, MatButtonModule, MatIconModule],
})
export class ProfileComponent {
  protected readonly authStore = inject(AuthStore);
  private readonly location = inject(Location);

  protected goBack(): void {
    this.location.back();
  }
}
