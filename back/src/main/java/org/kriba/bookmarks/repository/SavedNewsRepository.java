package org.kriba.bookmarks.repository;

import org.kriba.bookmarks.model.SavedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedNewsRepository extends JpaRepository<SavedArticle, Long> {
    List<SavedArticle> findAllByUserId(Long userid);
}
