import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { PostService } from '../../../core/services/post.service';
import { ThemeService } from '../../../core/services/theme.service';
import { ThemeResponse } from '../../../core/models';
import { SnackbarService } from '../../../core/services/snackbar.service';
import { ThemeSelectComponent } from './theme-select/theme-select.component';

interface CreatePostFormModel {
  themeId: string;
  title: string;
  content: string;
}

@Component({
  selector: 'app-create-post',
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, FormField, ThemeSelectComponent],
})
export class CreatePostComponent implements OnInit {
  private readonly postService = inject(PostService);
  private readonly themeService = inject(ThemeService);
  private readonly snackbar = inject(SnackbarService);
  private readonly router = inject(Router);

  protected readonly themes = signal<ThemeResponse[]>([]);

  protected readonly model = signal<CreatePostFormModel>({ themeId: '', title: '', content: '' });

  protected readonly createPostForm = form(this.model, (schemaPath) => {
    required(schemaPath.themeId, { message: 'Thème requis' });

    required(schemaPath.title, { message: 'Titre requis' });
    maxLength(schemaPath.title, 100, { message: '100 caractères maximum' });

    required(schemaPath.content, { message: 'Contenu requis' });
    maxLength(schemaPath.content, 5000, { message: '5000 caractères maximum' });
  });

  ngOnInit(): void {
    this.loadThemes();
  }

  protected goBack(): void {
    this.router.navigate(['/feed']);
  }

  protected onSubmit(event: Event): Promise<boolean> {
    event.preventDefault();
    return submit(this.createPostForm, async (field) => {
      try {
        const value = field().value();
        const post = await firstValueFrom(
          this.postService.create({
            themeId: Number(value.themeId),
            title: value.title,
            content: value.content,
          }),
        );
        this.snackbar.success('Article créé.');
        this.router.navigate(['/feed', post.id]);
      } catch (error) {
        this.snackbar.showApiError(error);
      }
    });
  }

  private async loadThemes(): Promise<void> {
    try {
      this.themes.set(await firstValueFrom(this.themeService.getAll()));
    } catch (error) {
      this.snackbar.showApiError(error);
    }
  }
}
