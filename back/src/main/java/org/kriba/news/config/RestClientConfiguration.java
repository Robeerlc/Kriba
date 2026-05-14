package org.kriba.news.config;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@NoArgsConstructor
public class RestClientConfiguration {

    @Bean
    public RestClient getRestClient(){
        return RestClient.create();
    }
}
