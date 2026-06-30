import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AuthStore } from '../../core/stores/auth.store';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule],
})
export class DashboardComponent {
  protected readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  protected logout(): void {
    this.authStore.logout().subscribe(() => this.router.navigate(['/login']));
  }
}
