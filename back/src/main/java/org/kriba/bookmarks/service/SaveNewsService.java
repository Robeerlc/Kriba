package org.kriba.bookmarks.service;

import org.kriba.analytics.model.Interaction;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.bookmarks.dto.ArticleInputDto;
import org.kriba.bookmarks.dto.ArticleResponseDto;
import org.kriba.bookmarks.model.SavedArticle;
import org.kriba.bookmarks.repository.SavedNewsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaveNewsService {

        private final SavedNewsRepository savedNewsRepository;
        private final InteractionRepository interactionRepository;

        public SaveNewsService(SavedNewsRepository savedNewsRepository, InteractionRepository interactionRepository){
            this.savedNewsRepository = savedNewsRepository;
            this.interactionRepository = interactionRepository;
        }
        
        public void saveNew(Long userId, ArticleInputDto savedNew){
            SavedArticle savedArticle = SavedArticle.builder()
                    .userId(userId)
                    .externalArticleId(savedNew.externalArticleId())
                    .title(savedNew.title())
                    .url(savedNew.url())
                    .build();    
            savedNewsRepository.save(savedArticle);

            Interaction interaction = Interaction.builder()
                    .userId(userId)
                    .articleCategory(savedNew.category())
                    .interactionType("SAVE")
                    .build();            
            interactionRepository.save(interaction);
        }

    public List<ArticleResponseDto> getSavedNews(Long userId){
           return savedNewsRepository.findAllByUserId(userId).stream()
                   .map(savedArticle -> ArticleResponseDto.builder()
                                .id(savedArticle.getId())
                                .externalArticleId(savedArticle.getExternalArticleId())
                                .title(savedArticle.getTitle())
                                .url(savedArticle.getUrl())
                                .timeStamp(savedArticle.getTimeStamp())
                                .build())
                   .toList();
    }
}