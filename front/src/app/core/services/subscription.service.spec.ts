import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { SubscriptionService } from './subscription.service';
import { environment } from '../../../environments/environment';
import { ThemeResponse } from '../models';

describe('SubscriptionService', () => {
  let service: SubscriptionService;
  let httpTesting: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/subscriptions`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SubscriptionService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('getMySubscriptions issues a GET request and returns the subscribed themes', async () => {
    const themes: ThemeResponse[] = [{ id: 1, title: 'Backend', description: 'desc', subscribed: true }];
    const promise = firstValueFrom(service.getMySubscriptions());

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(themes);

    await expect(promise).resolves.toEqual(themes);
  });

  it('subscribe issues a POST request to the theme subscription endpoint', async () => {
    const promise = firstValueFrom(service.subscribe(1));

    const req = httpTesting.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('POST');
    req.flush(null);

    await expect(promise).resolves.toBeNull();
  });

  it('unsubscribe issues a DELETE request to the theme subscription endpoint', async () => {
    const promise = firstValueFrom(service.unsubscribe(1));

    const req = httpTesting.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    await expect(promise).resolves.toBeNull();
  });
});
