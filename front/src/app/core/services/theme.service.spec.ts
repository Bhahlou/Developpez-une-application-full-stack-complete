import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { ThemeService } from './theme.service';
import { environment } from '../../../environments/environment';
import { ThemeResponse } from '../models';

describe('ThemeService', () => {
  let service: ThemeService;
  let httpTesting: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/themes`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ThemeService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('getAll issues a GET request and returns the themes', async () => {
    const themes: ThemeResponse[] = [{ id: 1, title: 'Backend', description: 'desc', subscribed: false }];
    const promise = firstValueFrom(service.getAll());

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(themes);

    await expect(promise).resolves.toEqual(themes);
  });

  it('create issues a POST request with the theme and returns the created theme', async () => {
    const request = { title: 'Backend', description: 'desc' };
    const created: ThemeResponse = { id: 1, title: 'Backend', description: 'desc', subscribed: false };
    const promise = firstValueFrom(service.create(request));

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(created);

    await expect(promise).resolves.toEqual(created);
  });
});
