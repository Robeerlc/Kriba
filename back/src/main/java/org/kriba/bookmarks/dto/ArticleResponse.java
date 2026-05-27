package org.kriba.bookmarks.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ArticleResponse(Long id, String externalArticleId, String title,String category, String url, String description,
                              String content, String image, LocalDateTime timeStamp) {}