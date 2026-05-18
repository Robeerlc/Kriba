package org.kriba.news.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NewsInfo {
    private String id;
    private String title;
    private String description;
    private String content;
    private String url;
    private String image;
    private String publishedAt;
    private String lang;
    private ResourceInfo source;
    private String category;
}