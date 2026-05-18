package org.kriba.news.model;



public record NewsInfo(String id, String title, String description, String content, String url,
                       String image, String publishedAt, String lang, ResourceInfo source, String category) {

}