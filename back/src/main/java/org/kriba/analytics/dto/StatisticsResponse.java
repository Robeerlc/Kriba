package org.kriba.analytics.dto;

import java.util.List;

public record StatisticsResponse(List<CategoryData> generalData, long totalArticlesRead) {
}