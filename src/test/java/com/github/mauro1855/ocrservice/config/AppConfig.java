package com.github.mauro1855.ocrservice.config;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.mauro1855.ocrservice.util.OCRThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by mauro1855 on 19/12/2016.
 */

// App config for tests
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

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(@Value("${pool.nb.threads}") int nbThread, @Value("${pool.queue.initial.size}") int 
      queueInitialSize) {
        return new OCRThreadPoolExecutor(nbThread, nbThread, 0L, TimeUnit.MILLISECONDS, queueInitialSize);
    }

    @Bean
    public boolean isRunningTests() {
        return true;
    }
}
