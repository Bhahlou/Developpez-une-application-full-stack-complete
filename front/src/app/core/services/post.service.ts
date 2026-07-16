import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreatePostRequest, PostPageResponse, PostResponse } from '../models';

export type PostSortDirection = 'asc' | 'desc';

@Service()
export class PostService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/posts`;

  getFeed(sort: PostSortDirection, page: number, size: number): Observable<PostPageResponse> {
    return this.http.get<PostPageResponse>(this.apiUrl, { params: { sort, page, size } });
  }

  getById(id: number): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.apiUrl}/${id}`);
  }

  create(request: CreatePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.apiUrl, request);
  }
}
