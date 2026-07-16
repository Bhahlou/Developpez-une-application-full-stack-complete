package com.openclassrooms.mddapi.dto;

import java.util.List;

/**
 * A page of the article feed, as returned to the client.
 *
 * @param content       the articles for this page
 * @param page          the zero-based index of this page
 * @param size          the requested page size
 * @param totalElements the total number of articles matching the feed
 * @param hasNext       whether another page can be fetched after this one
 */
public record PostPageResponse(
                List<PostResponse> content,
                int page,
                int size,
                long totalElements,
                boolean hasNext) {
}
