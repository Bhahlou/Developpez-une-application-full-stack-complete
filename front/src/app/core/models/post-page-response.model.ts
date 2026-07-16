import { PostResponse } from './post-response.model';

export interface PostPageResponse {
  content: PostResponse[];
  page: number;
  size: number;
  totalElements: number;
  hasNext: boolean;
}
