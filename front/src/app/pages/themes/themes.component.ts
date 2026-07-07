import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ThemeService } from '../../core/services/theme.service';
import { ThemeResponse } from '../../core/models';
import { SnackbarService } from '../../core/services/snackbar.service';
import { CreateThemeDialogComponent } from './create-theme-dialog/create-theme-dialog.component';

@Component({
  selector: 'app-themes',
  templateUrl: './themes.component.html',
  styleUrls: ['./themes.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule],
})
export class ThemesComponent implements OnInit {
  private readonly themeService = inject(ThemeService);
  private readonly snackbar = inject(SnackbarService);
  private readonly dialog = inject(MatDialog);

  protected readonly themes = signal<ThemeResponse[]>([]);
  protected readonly loading = signal(true);

  ngOnInit(): void {
    this.loadThemes();
  }

  protected openCreateDialog(): void {
    const dialogRef = this.dialog.open(CreateThemeDialogComponent);
    dialogRef.afterClosed().subscribe((created?: ThemeResponse) => {
      if (created) {
        this.themes.update((themes) =>
          [...themes, created].sort((a, b) => a.title.localeCompare(b.title)),
        );
        this.snackbar.success('Thème créé.');
      }
    });
  }

  private async loadThemes(): Promise<void> {
    this.loading.set(true);
    try {
      this.themes.set(await firstValueFrom(this.themeService.getAll()));
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.loading.set(false);
    }
  }
}
