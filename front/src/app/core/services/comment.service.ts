import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CommentResponse, CreateCommentRequest } from '../models';

@Service()
export class CommentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getByPostId(postId: number): Observable<CommentResponse[]> {
    return this.http.get<CommentResponse[]>(`${this.apiUrl}/posts/${postId}/comments`);
  }

  create(postId: number, request: CreateCommentRequest): Observable<CommentResponse> {
    return this.http.post<CommentResponse>(`${this.apiUrl}/posts/${postId}/comments`, request);
  }
}
