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

//    @PostConstruct
//    public void init(){
//        createBook();
//    }

    public void createBook() {
        // TODO
        log.info("Try book create");
        converter.convert("Если подъезжать к Ланну с юга, то с холмов его видно из далека. А слышно еще дальше. " +
                "Колокола Ланна известны на весь мир, что чтит Истинного Бога и Мать Церковь. " +
                "Издали город кажется огромным и прекрасным. Чистым и белым.");
        log.info("Book created");
    }
}
