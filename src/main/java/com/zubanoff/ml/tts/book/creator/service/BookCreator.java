package com.zubanoff.ml.tts.book.creator.service;

import com.zubanoff.ml.tts.book.creator.service.converter.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookCreator {

    private final Converter converter;

    public void createBook() {
        // TODO SPLIT BOOK TO CHAPTERS
        // TODO SPLIT CHAPTERS TO CHUNKS
        // TODO SEND CHUNKS TO CONVERTER
        // TODO MERGE MP3 CHUNKS TO MP3 CHAPTERS
//        log.info("Try book create");
//        converter.convert("Если подъезжать к Ланну с юга, то с холмов его видно из далека. А слышно еще дальше. " +
//                "Колокола Ланна известны на весь мир, что чтит Истинного Бога и Мать Церковь. " +
//                "Издали город кажется огромным и прекрасным. Чистым и белым.");
//        log.info("Book created");
    }
}
