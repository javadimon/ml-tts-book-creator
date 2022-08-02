package com.zubanoff.ml.tts.book.creator.service.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
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

    private final ApplicationContext applicationContext;

    public void convert(String chunkName, String text) {
        WebClient webClientWithTimeout = (WebClient) applicationContext.getBean("webClientWithTimeout");
        ConverterRequest convertRequest = new ConverterRequest();
        convertRequest.setText(text);

        Flux<DataBuffer> dataBuffer = webClientWithTimeout.post()
                .header("Authorization", "Bearer " + System.getenv("CONVERTER_TOKEN"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(convertRequest.getRequest())
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        Path destination = Paths.get(System.getProperty("user.dir"), "books", "out", "mp3", chunkName + "." + convertRequest.getFormat());
        DataBufferUtils.write(dataBuffer, destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).share().block();
        log.info("File {} created", chunkName + "." + convertRequest.getFormat());
    }
}
