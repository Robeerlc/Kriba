package org.kriba.news.model;


import java.util.List;

public record NewsTotalArticles(Long totalArticles, List<NewsInfo> articles) {

}
