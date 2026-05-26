package org.kriba.bookmarks.repository;

import org.kriba.bookmarks.model.SavedArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedNewsRepository extends JpaRepository<SavedArticle, Long> {
	List<SavedArticle> findAllByUserId(Long userid);

	Optional<SavedArticle> findByUserIdAndExternalArticleIdAndTitleAndCategoryAndDescriptionAndContentAndUrlAndImage(
			Long userId,
			String externalArticleId,
			String title,
			String category,
			String description,
			String content,
			String url,
			String image
			);	
}
