package org.kriba.bookmarks.service;

import org.kriba.analytics.model.Interaction;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.bookmarks.dto.ArticleInput;
import org.kriba.bookmarks.dto.ArticleResponse;
import org.kriba.bookmarks.model.SavedArticle;
import org.kriba.bookmarks.repository.SavedNewsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SaveNewsService {

    private final SavedNewsRepository savedNewsRepository;
    private final InteractionRepository interactionRepository;

    public SaveNewsService(SavedNewsRepository savedNewsRepository, InteractionRepository interactionRepository) {
        this.savedNewsRepository = savedNewsRepository;
        this.interactionRepository = interactionRepository;
    }

    public void saveNew(Long userId, ArticleInput savedNew) {
        if (savedNewsRepository.findByUserIdAndExternalArticleIdAndTitleAndCategoryAndDescriptionAndContentAndUrlAndImage(
                        userId,
                        savedNew.externalArticleId(),
                        savedNew.title(),
                        savedNew.category(),
                        savedNew.description(),
                        savedNew.content(),
                        savedNew.url(),
                        savedNew.image())
                .isPresent()) {
            throw new IllegalArgumentException("Artículo ya guardado");
        }
        SavedArticle savedArticle = SavedArticle
                .builder()
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

        if (interactionRepository.existsByUserIdAndExternalArticleIdAndInteractionType(
                userId,
                savedNew.externalArticleId(),
                "SAVE")) {
            throw new IllegalArgumentException("La interacción ya existe");
        }
        Interaction interaction = Interaction
                .builder()
                .userId(userId)
                .externalArticleId(savedNew.externalArticleId())
                .articleCategory(savedNew.category())
                .interactionType("SAVE")
                .build();
        interactionRepository.save(interaction);
    }

    public void unsave(Long userId, ArticleInput savedNew) {
        SavedArticle savedArticle = savedNewsRepository
                .findByUserIdAndExternalArticleIdAndTitleAndCategoryAndDescriptionAndContentAndUrlAndImage(
                        userId,
                        savedNew.externalArticleId(),
                        savedNew.title(),
                        savedNew.category(),
                        savedNew.description(),
                        savedNew.content(),
                        savedNew.url(),
                        savedNew.image())
                .orElseThrow(() -> new NoSuchElementException("Artículo no guardado"));
        savedNewsRepository.delete(savedArticle);

        Interaction interaction = interactionRepository
                .findByUserIdAndExternalArticleIdAndInteractionType(
                        userId,
                        savedNew.externalArticleId(),
                        "SAVE")
                .orElseThrow(() -> new NoSuchElementException("Interacción no encontrada"));
        interactionRepository.delete(interaction);
    }

    public List<ArticleResponse> getSavedNews(Long userId) {
        return savedNewsRepository.findAllByUserId(userId).stream()
                .map(savedArticle -> ArticleResponse.builder().id(savedArticle.getId())
                        .externalArticleId(savedArticle.getExternalArticleId())
                        .title(savedArticle.getTitle())
                        .category(savedArticle.getCategory())
                        .url(savedArticle.getUrl())
                        .description(savedArticle.getDescription())
                        .content(savedArticle.getContent()).image(savedArticle.getImage())
                        .timeStamp(savedArticle.getTimeStamp()).build())
                .toList();
    }
}