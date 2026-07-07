import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { PostService } from '../../../core/services/post.service';
import { CommentService } from '../../../core/services/comment.service';
import { CommentResponse, PostResponse } from '../../../core/models';
import { SnackbarService } from '../../../core/services/snackbar.service';

interface AddCommentFormModel {
  content: string;
}

@Component({
  selector: 'app-post-detail',
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    FormField,
    DatePipe,
  ],
})
export class PostDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly postService = inject(PostService);
  private readonly commentService = inject(CommentService);
  private readonly snackbar = inject(SnackbarService);

  protected readonly post = signal<PostResponse | null>(null);
  protected readonly loading = signal(true);

  protected readonly comments = signal<CommentResponse[]>([]);
  protected readonly commentsLoading = signal(true);

  protected readonly commentModel = signal<AddCommentFormModel>({ content: '' });
  protected readonly addCommentForm = form(this.commentModel, (schemaPath) => {
    required(schemaPath.content, { message: 'Commentaire requis' });
    maxLength(schemaPath.content, 1000, { message: '1000 caractères maximum' });
  });

  ngOnInit(): void {
    this.loadPost();
    this.loadComments();
  }

  protected goBack(): void {
    this.router.navigate(['/feed']);
  }

  protected onSubmitComment(event: Event): Promise<boolean> {
    event.preventDefault();
    const postId = this.postId;
    return submit(this.addCommentForm, async (field) => {
      try {
        const comment = await firstValueFrom(
          this.commentService.create(postId, { content: field().value().content }),
        );
        this.comments.update((comments) => [...comments, comment]);
        this.commentModel.set({ content: '' });
      } catch (error) {
        this.snackbar.showApiError(error);
      }
    });
  }

  private get postId(): number {
    return Number(this.route.snapshot.paramMap.get('id'));
  }

  private async loadPost(): Promise<void> {
    this.loading.set(true);
    try {
      this.post.set(await firstValueFrom(this.postService.getById(this.postId)));
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.loading.set(false);
    }
  }

  private async loadComments(): Promise<void> {
    this.commentsLoading.set(true);
    try {
      this.comments.set(await firstValueFrom(this.commentService.getByPostId(this.postId)));
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.commentsLoading.set(false);
    }
  }
}
