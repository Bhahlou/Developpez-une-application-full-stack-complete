import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { CommentService } from './comment.service';
import { environment } from '../../../environments/environment';
import { CommentResponse } from '../models';

describe('CommentService', () => {
  let service: CommentService;
  let httpTesting: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/posts/1/comments`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CommentService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('getByPostId issues a GET request and returns the comments', async () => {
    const comments: CommentResponse[] = [
      {
        id: 1,
        content: 'Nice article',
        authorUsername: 'johndoe',
        createdAt: '2026-01-01T00:00:00Z',
      },
    ];
    const promise = firstValueFrom(service.getByPostId(1));

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(comments);

    await expect(promise).resolves.toEqual(comments);
  });

  it('create issues a POST request with the comment and returns the created comment', async () => {
    const request = { content: 'Nice article' };
    const created: CommentResponse = {
      id: 1,
      content: 'Nice article',
      authorUsername: 'johndoe',
      createdAt: '2026-01-01T00:00:00Z',
    };
    const promise = firstValueFrom(service.create(1, request));

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(created);

    await expect(promise).resolves.toEqual(created);
  });
});
