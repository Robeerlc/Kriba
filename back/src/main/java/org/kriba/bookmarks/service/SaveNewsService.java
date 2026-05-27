package org.kriba.bookmarks.service;

import org.kriba.analytics.model.Interaction;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.bookmarks.dto.ArticleInput;
import org.kriba.bookmarks.dto.ArticleResponse;
import org.kriba.bookmarks.model.SavedArticle;
import org.kriba.bookmarks.repository.SavedNewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SaveNewsService {

    private final SavedNewsRepository savedNewsRepository;
    private final InteractionRepository interactionRepository;

    public SaveNewsService(SavedNewsRepository savedNewsRepository, InteractionRepository interactionRepository) {
        this.savedNewsRepository = savedNewsRepository;
        this.interactionRepository = interactionRepository;
    }

    @Transactional
    public void saveNew(Long userId, ArticleInput savedNew) {
        if (savedNewsRepository.findByUserIdAndExternalArticleId(userId, savedNew.externalArticleId()).isPresent()) return;
        
        SavedArticle savedArticle = SavedArticle.builder()
                .userId(userId)
                .externalArticleId(savedNew.externalArticleId())
                .title(savedNew.title())
                .category(savedNew.category())
                .url(savedNew.url())
                .description(savedNew.description())
                .content(savedNew.content())
                .image(savedNew.image())
                .build();
        savedNewsRepository.save(savedArticle);
        if (!interactionRepository.existsByUserIdAndExternalArticleIdAndInteractionType(userId, savedNew.externalArticleId(), "SAVE")) {
            Interaction interaction = Interaction.builder()
                    .userId(userId)
                    .externalArticleId(savedNew.externalArticleId())
                    .articleCategory(savedNew.category())
                    .interactionType("SAVE")
                    .build();
            interactionRepository.save(interaction);
        }
    }

    @Transactional
    public void unsave(Long userId, ArticleInput savedNew) {
        savedNewsRepository.findByUserIdAndExternalArticleId(userId, savedNew.externalArticleId())
                .ifPresent(savedNewsRepository::delete);
        interactionRepository.findByUserIdAndExternalArticleIdAndInteractionType(userId, savedNew.externalArticleId(), "SAVE")
                .ifPresent(interactionRepository::delete);
    }

    public List<ArticleResponse> getSavedNews(Long userId) {
        return savedNewsRepository.findAllByUserId(userId).stream()
                .map(savedArticle -> ArticleResponse.builder()
                        .id(savedArticle.getId())
                        .externalArticleId(savedArticle.getExternalArticleId())
                        .title(savedArticle.getTitle())
                        .category(savedArticle.getCategory())
                        .url(savedArticle.getUrl())
                        .description(savedArticle.getDescription())
                        .content(savedArticle.getContent())
                        .image(savedArticle.getImage())
                        .timeStamp(savedArticle.getTimeStamp())
                        .build())
                .toList();
    }
}
