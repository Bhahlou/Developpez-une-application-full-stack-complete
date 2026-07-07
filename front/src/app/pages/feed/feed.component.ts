import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PostService, PostSortDirection } from '../../core/services/post.service';
import { PostResponse } from '../../core/models';
import { SnackbarService } from '../../core/services/snackbar.service';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule, RouterLink, DatePipe],
})
export class FeedComponent implements OnInit {
  private readonly postService = inject(PostService);
  private readonly snackbar = inject(SnackbarService);

  protected readonly posts = signal<PostResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly sortDirection = signal<PostSortDirection>('desc');

  ngOnInit(): void {
    this.loadFeed();
  }

  protected toggleSort(): void {
    this.sortDirection.update((direction) => (direction === 'desc' ? 'asc' : 'desc'));
    this.loadFeed();
  }

  private async loadFeed(): Promise<void> {
    this.loading.set(true);
    try {
      this.posts.set(await firstValueFrom(this.postService.getFeed(this.sortDirection())));
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.loading.set(false);
    }
  }
}
