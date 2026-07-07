import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FeedComponent } from './feed.component';
import { PostService } from '../../core/services/post.service';
import { SnackbarService } from '../../core/services/snackbar.service';

describe('FeedComponent', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;
  let postService: { getFeed: ReturnType<typeof vi.fn> };
  let snackbar: { showApiError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    postService = { getFeed: vi.fn().mockReturnValue(of([])) };
    snackbar = { showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: postService },
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('loads the feed sorted by descending date by default', () => {
    fixture.detectChanges();

    expect(postService.getFeed).toHaveBeenCalledWith('desc');
  });

  it('shows an API error when loading the feed fails', async () => {
    const error = new Error('failed');
    postService.getFeed.mockReturnValue(throwError(() => error));

    await component['loadFeed']();

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
  });

  it('toggles the sort direction from desc to asc and reloads the feed', () => {
    fixture.detectChanges();
    postService.getFeed.mockClear();

    component['toggleSort']();

    expect(component['sortDirection']()).toBe('asc');
    expect(postService.getFeed).toHaveBeenCalledWith('asc');
  });

  it('toggles the sort direction from asc back to desc and reloads the feed', () => {
    fixture.detectChanges();
    component['toggleSort']();
    postService.getFeed.mockClear();

    component['toggleSort']();

    expect(component['sortDirection']()).toBe('desc');
    expect(postService.getFeed).toHaveBeenCalledWith('desc');
  });
});
