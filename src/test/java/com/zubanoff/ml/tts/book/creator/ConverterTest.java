package com.zubanoff.ml.tts.book.creator;

import com.zubanoff.ml.tts.book.creator.service.BookCreatorService;
import com.zubanoff.ml.tts.book.creator.service.converter.Converter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@Slf4j
public class ConverterTest {

    @Autowired
    public Converter converter;

    @Autowired
    public BookCreatorService bookCreatorService;


    @Disabled
    @Test
    @SneakyThrows
    public void convertTest(){
//        String text = """
//                Если подъезжать к Ланну с юга, то с холмов его видно из далека. А слышно еще дальше. Колокола Ланна известны на весь мир, что чтит Истинного Бога и Мать Церковь. Издали город кажется огромным и прекрасным. Чистым и белым.""";
////        converter.convert(text);
//
//        Thread.sleep(10000);
    }

    @Disabled
    @Test
    public void splitBookToChaptersTest(){
        assertThat(bookCreatorService, notNullValue());
        Path bookPath = Paths.get(System.getProperty("user.dir"), "books", "source", "txt", "Мощи Святого Леопольда - Борис Конофальский.txt");
        TreeMap<Integer, List<String>> chapters = bookCreatorService.splitBookToChapters(bookPath);
        assertThat(chapters.size(), greaterThan(0));

        List<TreeMap<String, String>> chunks = bookCreatorService.splitChaptersToChunks(chapters);
        assertThat(chunks.size(), greaterThan(0));

        int totalSymbols = 0;
        for(TreeMap<String, String> chunk : chunks){
            for(Map.Entry<String, String> entry : chunk.entrySet()){
                log.info("{} - {}", entry.getKey(), entry.getValue().length());
                totalSymbols += entry.getValue().length();
            }
        }

        log.info("Total symbols: {}", totalSymbols);
    }
}
