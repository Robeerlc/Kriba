package org.kriba.bookmarks.dto;

public record ArticleInput(String externalArticleId, String title, String url, String category, String description, String content, String image) {
}
