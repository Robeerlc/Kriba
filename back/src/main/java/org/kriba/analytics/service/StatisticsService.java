package org.kriba.analytics.service;

import org.kriba.analytics.dto.CategoryData;
import org.kriba.analytics.dto.StatisticsResponse;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.analytics.repository.InteractionRepository.CategoryStats;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService {

    private final InteractionRepository interactionRepository;

    public StatisticsService(InteractionRepository interactionRepository) {
        this.interactionRepository = interactionRepository;
    }

    public StatisticsResponse getUserStatistics(Long userId) {
        List<CategoryStats> stats = interactionRepository.getCategoryStatsByUserId(userId);
        long totalPoints = stats.stream().mapToLong(CategoryStats::getPoints).sum();
        long totalArticles = stats.stream().mapToLong(CategoryStats::getArticlesRead).sum();
        if (totalPoints == 0) return new StatisticsResponse(List.of(), 0);
        List<CategoryData> data = stats.stream()
                .map(stat -> new CategoryData(
                        stat.getCategory(),
                        Math.round(stat.getPoints() * 10000.0 / totalPoints) / 100.0
                )).toList();
        return new StatisticsResponse(data, totalArticles);
    }
}