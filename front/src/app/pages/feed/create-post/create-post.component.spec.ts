import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CreatePostComponent } from './create-post.component';
import { PostService } from '../../../core/services/post.service';
import { ThemeService } from '../../../core/services/theme.service';
import { SnackbarService } from '../../../core/services/snackbar.service';

describe('CreatePostComponent', () => {
  let component: CreatePostComponent;
  let fixture: ComponentFixture<CreatePostComponent>;
  let postService: { create: ReturnType<typeof vi.fn> };
  let themeService: { getAll: ReturnType<typeof vi.fn> };
  let snackbar: { success: ReturnType<typeof vi.fn>; showApiError: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    postService = { create: vi.fn() };
    themeService = { getAll: vi.fn().mockReturnValue(of([{ id: 1, title: 'Backend', description: 'desc', subscribed: false }])) };
    snackbar = { success: vi.fn(), showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [CreatePostComponent],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: postService },
        { provide: ThemeService, useValue: themeService },
        { provide: SnackbarService, useValue: snackbar },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(CreatePostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the themes on init', () => {
    expect(themeService.getAll).toHaveBeenCalled();
    expect(component['themes']()).toEqual([{ id: 1, title: 'Backend', description: 'desc', subscribed: false }]);
  });

  it('navigates back to the feed when cancelled', () => {
    component['goBack']();

    expect(router.navigate).toHaveBeenCalledWith(['/feed']);
  });

  it('does not call the API when the form is invalid', async () => {
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(postService.create).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('creates the post and navigates to its detail page on success', async () => {
    component['model'].set({ themeId: '1', title: 'Title', content: 'Content' });
    const created = {
      id: 5,
      title: 'Title',
      content: 'Content',
      themeId: 1,
      themeTitle: 'Backend',
      authorUsername: 'johndoe',
      createdAt: '2026-01-01T00:00:00Z',
    };
    postService.create.mockReturnValue(of(created));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(postService.create).toHaveBeenCalledWith({ themeId: 1, title: 'Title', content: 'Content' });
    expect(snackbar.success).toHaveBeenCalledWith('Article créé.');
    expect(router.navigate).toHaveBeenCalledWith(['/feed', 5]);
  });

  it('shows an API error and does not navigate on failure', async () => {
    component['model'].set({ themeId: '1', title: 'Title', content: 'Content' });
    const error = new Error('failed');
    postService.create.mockReturnValue(throwError(() => error));
    const event = new Event('submit');

    await component['onSubmit'](event);

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('shows an API error when loading themes fails', async () => {
    const error = new Error('failed');
    themeService.getAll.mockReturnValue(throwError(() => error));

    await component['loadThemes']();

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
  });
});
