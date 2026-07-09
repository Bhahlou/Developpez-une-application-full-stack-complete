import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateThemeRequest, ThemeResponse } from '../models';

@Service()
export class ThemeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/themes`;

  getAll(): Observable<ThemeResponse[]> {
    return this.http.get<ThemeResponse[]>(this.apiUrl);
  }

  create(request: CreateThemeRequest): Observable<ThemeResponse> {
    return this.http.post<ThemeResponse>(this.apiUrl, request);
  }
}
