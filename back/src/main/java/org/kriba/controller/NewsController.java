package org.kriba.controller;

import lombok.AllArgsConstructor;
import org.kriba.model.NewsTotalArticles;
import org.kriba.service.NewsServiceInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api")
public class NewsController {

    private final NewsServiceInfo newsService;

    @GetMapping(path = "/feed")
    public NewsTotalArticles getAllNewsData(@RequestParam(name = "page", required = false) Long page){
        return newsService.getNewsInfo();
    }
}
