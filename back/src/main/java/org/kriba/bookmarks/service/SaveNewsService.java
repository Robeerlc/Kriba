package org.kriba.bookmarks.service;

import org.kriba.bookmarks.dto.SaveRequest;
import org.kriba.bookmarks.dto.SavedArticleInfo;
import org.kriba.bookmarks.model.SavedArticle;
import org.kriba.bookmarks.repository.SavedNewsRepository;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.model.User;
import org.kriba.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaveNewsService {

        private final SavedNewsRepository savedNewsRepository;
        private final UserRepository userRepository;

        public SaveNewsService(SavedNewsRepository savedNewsRepository, UserRepository userRepository){
            this.savedNewsRepository = savedNewsRepository;
            this.userRepository = userRepository;
        }


        public void saveNew(SaveRequest saveRequest){
            User user = userRepository.findByEmail(saveRequest.loginRequest().email())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            SavedArticle savedArticle = SavedArticle.builder()
                    .userId(user.getId())
                    .externalArticleId(saveRequest.savedNew().externalArticleId())
                    .title(saveRequest.savedNew().title())
                    .url(saveRequest.savedNew().url())
                    .build();
            savedNewsRepository.save(savedArticle);
        }

    public List<SavedArticleInfo> getSavedNews(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

           return savedNewsRepository.findAllByUserId(user.getId())
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
