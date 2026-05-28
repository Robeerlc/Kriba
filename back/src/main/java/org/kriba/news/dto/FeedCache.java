package org.kriba.news.dto;

import java.util.List;

public record FeedCache(List<NewsInfo> articles, String newestPublishedAt, String oldestPublishedAt, Integer lastDeliveredIndex) {
}
