package org.kriba.analytics.repository;

import org.kriba.analytics.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    @Query("SELECT i.articleCategory, " +
            "SUM(CASE i.interactionType " +
            "  WHEN 'SAVE' THEN 3 " +
            "  WHEN 'SUMMARIZE' THEN 2 " +
            "  ELSE 1 END) " +
            "FROM Interaction i WHERE i.userId = :userId GROUP BY i.articleCategory")
    List<Object[]> countCategoriesByUserId(@Param("userId") Long userId);
}