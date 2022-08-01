package com.zubanoff.ml.tts.book.creator.service;

import com.zubanoff.ml.tts.book.creator.service.converter.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookCreator {

    private final Converter converter;
    private static final String CHAPTER_SPLITTER = "глава";
    private static final String SHORT_NEW_LINE = "\n";
    private static final String FULL_NEW_LINE = "\r\n";

    public void createBook(Path bookPath) {
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

    @SneakyThrows
    public Map<Integer, List<String>> splitBookToChapters(Path bookPath) {
        Map<Integer, List<String>> chapters = new TreeMap<>();
        List<String> lines = Files.readAllLines(bookPath);
        int chapterNumber = 1;
        int count = 0;
        for (String line : lines) {
            if (line.toLowerCase().startsWith(CHAPTER_SPLITTER) && count == 0) {
                if (lines.get(count + 1).startsWith(SHORT_NEW_LINE) || lines.get(count + 1).startsWith(FULL_NEW_LINE)) {
                    chapters.put(chapterNumber, List.of(line));
                    chapterNumber++;
                }
            } else if (line.toLowerCase().startsWith(CHAPTER_SPLITTER) && count > 0 && count + 1 < lines.size()) {
                if ((lines.get(count - 1).startsWith(SHORT_NEW_LINE) || lines.get(count - 1).startsWith(FULL_NEW_LINE)) &&
                        (lines.get(count + 1).startsWith(SHORT_NEW_LINE) || lines.get(count + 1).startsWith(FULL_NEW_LINE))) {
                    chapters.put(chapterNumber, List.of(line));
                    chapterNumber++;
                }
            } else {
                // TODO
            }

            count++;
        }

        return chapters;
    }
}
