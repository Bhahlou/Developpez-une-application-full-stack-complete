import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PostDetailComponent } from './post-detail.component';
import { PostService } from '../../../core/services/post.service';
import { SnackbarService } from '../../../core/services/snackbar.service';

describe('PostDetailComponent', () => {
  let component: PostDetailComponent;
  let fixture: ComponentFixture<PostDetailComponent>;
  let postService: { getById: ReturnType<typeof vi.fn> };
  let snackbar: { showApiError: ReturnType<typeof vi.fn> };
  let router: Router;

  const post = {
    id: 1,
    title: 'Title',
    content: 'Content',
    themeId: 2,
    themeTitle: 'Backend',
    authorUsername: 'johndoe',
    createdAt: '2026-01-01T00:00:00Z',
  };

  beforeEach(async () => {
    postService = { getById: vi.fn().mockReturnValue(of(post)) };
    snackbar = { showApiError: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [PostDetailComponent],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: postService },
        { provide: SnackbarService, useValue: snackbar },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } },
        },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(PostDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('loads the post matching the route id', async () => {
    await component['loadPost']();

    expect(postService.getById).toHaveBeenCalledWith(1);
    expect(component['post']()).toEqual(post);
    expect(component['loading']()).toBe(false);
  });

  it('shows an API error when loading the post fails', async () => {
    const error = new Error('failed');
    postService.getById.mockReturnValue(throwError(() => error));

    await component['loadPost']();

    expect(snackbar.showApiError).toHaveBeenCalledWith(error);
    expect(component['loading']()).toBe(false);
  });

  it('navigates back to the feed', () => {
    fixture.detectChanges();

    component['goBack']();

    expect(router.navigate).toHaveBeenCalledWith(['/feed']);
  });
});
