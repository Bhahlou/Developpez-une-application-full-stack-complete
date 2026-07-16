import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { PostService } from './post.service';
import { environment } from '../../../environments/environment';
import { PostPageResponse, PostResponse } from '../models';

describe('PostService', () => {
  let service: PostService;
  let httpTesting: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/posts`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PostService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('getFeed issues a GET request with the sort, page and size params and returns the page', async () => {
    const page: PostPageResponse = {
      content: [
        {
          id: 1,
          title: 'Title',
          content: 'Content',
          themeId: 2,
          themeTitle: 'Backend',
          authorUsername: 'johndoe',
          createdAt: '2026-01-01T00:00:00Z',
        },
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      hasNext: false,
    };
    const promise = firstValueFrom(service.getFeed('desc', 0, 10));

    const req = httpTesting.expectOne((request) => request.url === apiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('sort')).toBe('desc');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    req.flush(page);

    await expect(promise).resolves.toEqual(page);
  });

  it('getById issues a GET request and returns the post', async () => {
    const post: PostResponse = {
      id: 1,
      title: 'Title',
      content: 'Content',
      themeId: 2,
      themeTitle: 'Backend',
      authorUsername: 'johndoe',
      createdAt: '2026-01-01T00:00:00Z',
    };
    const promise = firstValueFrom(service.getById(1));

    const req = httpTesting.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(post);

    await expect(promise).resolves.toEqual(post);
  });

  it('create issues a POST request with the post and returns the created post', async () => {
    const request = { themeId: 2, title: 'Title', content: 'Content' };
    const created: PostResponse = {
      id: 1,
      title: 'Title',
      content: 'Content',
      themeId: 2,
      themeTitle: 'Backend',
      authorUsername: 'johndoe',
      createdAt: '2026-01-01T00:00:00Z',
    };
    const promise = firstValueFrom(service.create(request));

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(created);

    await expect(promise).resolves.toEqual(created);
  });
});
