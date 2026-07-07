import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ThemeResponse } from '../models';

@Service()
export class SubscriptionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/subscriptions`;

  getMySubscriptions(): Observable<ThemeResponse[]> {
    return this.http.get<ThemeResponse[]>(this.apiUrl);
  }

  subscribe(themeId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${themeId}`, null);
  }

  unsubscribe(themeId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${themeId}`);
  }
}
