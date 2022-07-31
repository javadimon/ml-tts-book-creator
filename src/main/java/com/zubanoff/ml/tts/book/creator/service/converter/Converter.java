package com.zubanoff.ml.tts.book.creator.service.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
@Component
@Slf4j
public class Converter {

    private final WebClient webClientWithTimeout;

    public void convert(String text) {
        ConverterRequest convertRequest = new ConverterRequest();
        convertRequest.setText(text);
        log.info("Request: {}", convertRequest.getRequest());

        Flux<DataBuffer> dataBuffer = webClientWithTimeout.post()
                .header("Authorization", "Bearer t1.9euelZrNzYnGzcielp6VicyRnciez-3rnpWazcuYjpzJnpyXlJiYkM_Mkp7l9PdtWWVo-e8PQSeS3fT3LQhjaPnvD0Enkg.EIp9Pp213-lDREtNfN0ieJP98mFnALbawxeyuXeTmACqF6zWdIkMnU3mga_ZLOrdEQIYavLrf1r0P7T1ttt7BQ")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(convertRequest.getRequest())
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        Path destination = Paths.get(System.getProperty("user.dir"), "books", "out", "mp3", "test.mp3");
        DataBufferUtils.write(dataBuffer, destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).share().block();
    }
}
