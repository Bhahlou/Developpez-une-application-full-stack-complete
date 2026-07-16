import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FeedComponent } from './feed.component';
import { PostService } from '../../core/services/post.service';
import { SnackbarService } from '../../core/services/snackbar.service';
import { PostPageResponse, PostResponse } from '../../core/models';

class IntersectionObserverMock {
  static instances: IntersectionObserverMock[] = [];
  readonly observe = vi.fn();
  readonly disconnect = vi.fn();
  readonly unobserve = vi.fn();

  constructor(private readonly callback: IntersectionObserverCallback) {
    IntersectionObserverMock.instances.push(this);
  }

  trigger(isIntersecting: boolean): void {
    this.callback(
      [{ isIntersecting } as IntersectionObserverEntry],
      this as unknown as IntersectionObserver,
    );
  }
}

function emptyPage(): PostPageResponse {
  return { content: [], page: 0, size: 10, totalElements: 0, hasNext: false };
}

function post(id: number, title: string): PostResponse {
  return {
    id,
    title,
    content: 'Content',
    themeId: 2,
    themeTitle: 'Backend',
    authorUsername: 'johndoe',
    createdAt: '2026-01-01T00:00:00Z',
  };
}

describe('FeedComponent', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;
  let postService: { getFeed: ReturnType<typeof vi.fn> };
  let snackbar: { showApiError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    IntersectionObserverMock.instances = [];
    vi.stubGlobal('IntersectionObserver', IntersectionObserverMock);

    postService = { getFeed: vi.fn().mockReturnValue(of(emptyPage())) };
    snackbar = { showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: PostService, useValue: postService },
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('loads the first page sorted by descending date by default', () => {
    fixture.detectChanges();

    expect(postService.getFeed).toHaveBeenCalledWith('desc', 0, 10);
  });

  it('shows an API error when loading the feed fails', async () => {
    const error = new Error('failed');
    postService.getFeed.mockReturnValue(throwError(() => error));

    await component['loadFeed']();

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
  });

  it('toggles the sort direction from desc to asc and reloads the first page', () => {
    fixture.detectChanges();
    postService.getFeed.mockClear();

    component['toggleSort']();

    expect(component['sortDirection']()).toBe('asc');
    expect(postService.getFeed).toHaveBeenCalledWith('asc', 0, 10);
  });

  it('toggles the sort direction from asc back to desc and reloads the first page', () => {
    fixture.detectChanges();
    component['toggleSort']();
    postService.getFeed.mockClear();

    component['toggleSort']();

    expect(component['sortDirection']()).toBe('desc');
    expect(postService.getFeed).toHaveBeenCalledWith('desc', 0, 10);
  });

  it('appends the next page and stops requesting once the feed is exhausted', async () => {
    const first = post(1, 'First');
    const second = post(2, 'Second');
    component['posts'].set([first]);
    component['hasNext'].set(true);
    component['nextPage'] = 1;
    postService.getFeed.mockReturnValue(of({ content: [second], page: 1, size: 10, totalElements: 2, hasNext: false }));

    await component['loadMore']();

    expect(postService.getFeed).toHaveBeenCalledWith('desc', 1, 10);
    expect(component['posts']()).toEqual([first, second]);
    expect(component['hasNext']()).toBe(false);
  });

  it('does not request another page when there is none left', async () => {
    component['hasNext'].set(false);
    postService.getFeed.mockClear();

    await component['loadMore']();

    expect(postService.getFeed).not.toHaveBeenCalled();
  });

  it('does not request another page while one is already loading', async () => {
    component['hasNext'].set(true);
    component['loadingMore'].set(true);
    postService.getFeed.mockClear();

    await component['loadMore']();

    expect(postService.getFeed).not.toHaveBeenCalled();
  });

  it('triggers loadMore when the scroll sentinel intersects the viewport', async () => {
    const first = post(1, 'First');
    const second = post(2, 'Second');
    postService.getFeed.mockReturnValueOnce(
      of({ content: [first], page: 0, size: 10, totalElements: 2, hasNext: true }),
    );
    postService.getFeed.mockReturnValueOnce(
      of({ content: [second], page: 1, size: 10, totalElements: 2, hasNext: false }),
    );

    fixture.detectChanges();
    await fixture.whenStable();
    TestBed.tick();

    expect(IntersectionObserverMock.instances).toHaveLength(1);
    IntersectionObserverMock.instances[0].trigger(true);
    await fixture.whenStable();
    TestBed.tick();

    expect(postService.getFeed).toHaveBeenCalledWith('desc', 1, 10);
    expect(component['posts']()).toEqual([first, second]);
  });
});
