package com.zubanoff.ml.tts.book.creator.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {

    private static final String BASE_URL = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize";
    public static final int READ_TIMEOUT = 300000;
    public static final int WRITE_TIMEOUT = 10000;

    @Bean
    public WebClient webClientWithTimeout() {
        HttpClient httpClient = HttpClient.create()
                .doOnConnected(connection -> {
                    connection.addHandlerFirst(new ReadTimeoutHandler(READ_TIMEOUT, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT, TimeUnit.MILLISECONDS));
                });

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(BASE_URL)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer ->
                                configurer.defaultCodecs()
                                        .maxInMemorySize(5 * 1024000)
                        )
                        .build())
                .build();
    }
}
