package org.kriba.bookmarks.dto;

import lombok.Builder;

@Builder
public record ArticleResponse(Long id, String externalArticleId, String title, String url, String description, String content,String image) {
}
