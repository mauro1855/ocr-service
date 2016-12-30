package com.github.mauro1855.ocrservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by pereirat on 19/12/2016.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public Runtime runtime(){
        return Runtime.getRuntime();
    }
}
