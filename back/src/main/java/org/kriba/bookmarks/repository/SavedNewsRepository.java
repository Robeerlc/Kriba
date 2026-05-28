package org.kriba.bookmarks.repository;

import org.kriba.bookmarks.model.SavedArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface SavedNewsRepository extends JpaRepository<SavedArticle, Long> {
    Page<SavedArticle> findAllByUserId(Long userid, Pageable pageable);

    boolean existsByUserIdAndExternalArticleId(Long userId, String externalArticleId);

    Optional<SavedArticle> findByUserIdAndExternalArticleId(Long userId, String externalArticleId);
}
