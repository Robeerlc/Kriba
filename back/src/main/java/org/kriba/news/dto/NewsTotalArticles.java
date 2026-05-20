package org.kriba.news.dto;

import java.util.List;

public record NewsTotalArticles(Long totalArticles, List<NewsInfo> articles) {}
