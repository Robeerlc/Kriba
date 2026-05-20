package org.kriba.bookmarks.dto;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record SavedArticleInfo(Long id, String externalArticleId, String title, String url, LocalDateTime timeStamp) {
}
