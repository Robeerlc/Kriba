package org.kriba.bookmarks.service;

import org.kriba.analytics.model.Interaction;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.bookmarks.dto.SaveRequest;
import org.kriba.bookmarks.dto.SavedArticleInfo;
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


        public void saveNew(Long userId,SaveRequest saveRequest){
            SavedArticle savedArticle = SavedArticle.builder()
                    .userId(userId)
                    .externalArticleId(saveRequest.savedNew().externalArticleId())
                    .title(saveRequest.savedNew().title())
                    .url(saveRequest.savedNew().url())
                    .build();

            Interaction interaction = Interaction.builder()
                    .userId(userId)
                    .articleCategory(saveRequest.savedNew().category())
                    .interactionType("SAVE")
                    .build();


            savedNewsRepository.save(savedArticle);
            interactionRepository.save(interaction);
        }

    public List<SavedArticleInfo> getSavedNews(Long userId){


           return savedNewsRepository.findAllByUserId(userId)
                   .stream()
                   .map(savedArticle -> SavedArticleInfo.builder()
                            .id(savedArticle.getId())
                           .externalArticleId(savedArticle.getExternalArticleId())
                           .title(savedArticle.getTitle())
                           .url(savedArticle.getUrl())
                           .timeStamp(savedArticle.getTimeStamp())
                           .build())
                   .toList();


    }
}
