import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthStore } from '../../core/stores/auth.store';
import { SubscriptionService } from '../../core/services/subscription.service';
import { SnackbarService } from '../../core/services/snackbar.service';
import { ThemeResponse } from '../../core/models';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [RouterLink, MatButtonModule, MatDividerModule, MatIconModule, MatProgressSpinnerModule],
})
export class ProfileComponent implements OnInit {
  protected readonly authStore = inject(AuthStore);
  private readonly location = inject(Location);
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly snackbar = inject(SnackbarService);

  protected readonly subscriptions = signal<ThemeResponse[]>([]);
  protected readonly loadingSubscriptions = signal(true);

  ngOnInit(): void {
    this.loadSubscriptions();
  }

  protected goBack(): void {
    this.location.back();
  }

  protected async unsubscribe(theme: ThemeResponse): Promise<void> {
    try {
      await firstValueFrom(this.subscriptionService.unsubscribe(theme.id));
      this.subscriptions.update((subscriptions) => subscriptions.filter((t) => t.id !== theme.id));
      this.snackbar.success('Désabonnement effectué.');
    } catch (error) {
      this.snackbar.showApiError(error);
    }
  }

  private async loadSubscriptions(): Promise<void> {
    this.loadingSubscriptions.set(true);
    try {
      this.subscriptions.set(await firstValueFrom(this.subscriptionService.getMySubscriptions()));
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.loadingSubscriptions.set(false);
    }
  }
}
