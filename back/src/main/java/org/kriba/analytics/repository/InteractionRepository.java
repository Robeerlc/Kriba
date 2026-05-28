package org.kriba.analytics.repository;

import org.kriba.analytics.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    @Query("SELECT i.articleCategory as category, "
            + "SUM(CASE WHEN i.interactionType = 'SAVE' THEN 3 "
            + " WHEN i.interactionType = 'SUMMARIZE' THEN 2 "
            + " ELSE 1 END) as points, "
            + "SUM(CASE WHEN i.interactionType = 'CLICK' THEN 1 ELSE 0 END) as articlesRead "
            + "FROM Interaction i WHERE i.userId = :userId GROUP BY i.articleCategory")
    List<CategoryStats> getCategoryStatsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndExternalArticleIdAndInteractionType(
            Long userId,
            String externalArticleId,
            String interactionType
    );

    Optional<Interaction> findByUserIdAndExternalArticleIdAndInteractionType(
            Long userId,
            String externalArticleId,
            String interactionType
    );

    void deleteAllByUserId(long userId);

    interface CategoryStats {
        String getCategory();

        Long getPoints();

        Long getArticlesRead();
    }
}
