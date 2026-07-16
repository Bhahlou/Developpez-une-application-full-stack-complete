import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  OnDestroy,
  ElementRef,
  inject,
  signal,
  viewChild,
  effect,
} from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PostService, PostSortDirection } from '../../core/services/post.service';
import { PostResponse } from '../../core/models';
import { SnackbarService } from '../../core/services/snackbar.service';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule, RouterLink, DatePipe],
})
export class FeedComponent implements OnInit, OnDestroy {
  private readonly postService = inject(PostService);
  private readonly snackbar = inject(SnackbarService);

  protected readonly posts = signal<PostResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadingMore = signal(false);
  protected readonly hasNext = signal(false);
  protected readonly sortDirection = signal<PostSortDirection>('desc');

  private readonly sentinel = viewChild<ElementRef<HTMLElement>>('sentinel');
  private observer?: IntersectionObserver;
  private nextPage = 0;

  constructor() {
    effect(() => {
      const element = this.sentinel()?.nativeElement;
      this.observer?.disconnect();
      if (!element) {
        return;
      }
      this.observer = new IntersectionObserver(
        (entries) => {
          if (entries[0].isIntersecting) {
            this.loadMore();
          }
        },
        { rootMargin: '200px' },
      );
      this.observer.observe(element);
    });
  }

  ngOnInit(): void {
    this.loadFeed();
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  protected toggleSort(): void {
    this.sortDirection.update((direction) => (direction === 'desc' ? 'asc' : 'desc'));
    this.loadFeed();
  }

  private async loadFeed(): Promise<void> {
    this.loading.set(true);
    try {
      const response = await firstValueFrom(this.postService.getFeed(this.sortDirection(), 0, PAGE_SIZE));
      this.posts.set(response.content);
      this.hasNext.set(response.hasNext);
      this.nextPage = 1;
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.loading.set(false);
    }
  }

  private async loadMore(): Promise<void> {
    if (this.loadingMore() || !this.hasNext()) {
      return;
    }
    this.loadingMore.set(true);
    try {
      const response = await firstValueFrom(
        this.postService.getFeed(this.sortDirection(), this.nextPage, PAGE_SIZE),
      );
      this.posts.update((posts) => [...posts, ...response.content]);
      this.hasNext.set(response.hasNext);
      this.nextPage += 1;
    } catch (error) {
      this.snackbar.showApiError(error);
    } finally {
      this.loadingMore.set(false);
    }
  }
}
