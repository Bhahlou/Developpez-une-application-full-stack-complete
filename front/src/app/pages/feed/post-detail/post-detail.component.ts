import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PostService } from '../../../core/services/post.service';
import { PostResponse } from '../../../core/models';
import { SnackbarService } from '../../../core/services/snackbar.service';

@Component({
  selector: 'app-post-detail',
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule, DatePipe],
})
export class PostDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly postService = inject(PostService);
  private readonly snackbar = inject(SnackbarService);

  protected readonly post = signal<PostResponse | null>(null);
  protected readonly loading = signal(true);

  ngOnInit(): void {
    this.loadPost();
  }

  protected goBack(): void {
    this.router.navigate(['/feed']);
  }

  private async loadPost(): Promise<void> {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading.set(true);
    try {
      this.post.set(await firstValueFrom(this.postService.getById(id)));
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.loading.set(false);
    }
  }
}
