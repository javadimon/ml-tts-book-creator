package com.zubanoff.ml.tts.book.creator.service;

import com.zubanoff.ml.tts.book.creator.service.converter.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookCreator {

    private final Converter converter;
    private static final String CHAPTER_SPLITTER = "глава";
    private static final String EMPTY_LINE = "\n";
    private static final int MAX_CHUNK_LENGTH = 5000;

    public void createBook(Path bookPath) {
        TreeMap<Integer, List<String>> chapters = splitBookToChapters(bookPath);
        splitChaptersToChunks(chapters);

        // TODO SEND CHUNKS TO CONVERTER
        // TODO MERGE MP3 CHUNKS TO MP3 CHAPTERS
//        log.info("Try book create");
//        converter.convert("Если подъезжать к Ланну с юга, то с холмов его видно из далека. А слышно еще дальше. " +
//                "Колокола Ланна известны на весь мир, что чтит Истинного Бога и Мать Церковь. " +
//                "Издали город кажется огромным и прекрасным. Чистым и белым.");
//        log.info("Book created");
    }

    public List<TreeMap<String, String>> splitChaptersToChunks(TreeMap<Integer, List<String>> chapters) {
        List<TreeMap<String, String>> chunks = new ArrayList<>();

        for(Map.Entry<Integer, List<String>> chapter : chapters.entrySet()) {
            int chapterNumber = chapter.getKey();
            int chapterSubNumber = 1;

            TreeMap<String, String> map = new TreeMap<>();
            StringBuilder chapterChunkText = new StringBuilder();
            for(String chapterLine : chapter.getValue()) {

                chapterLine = chapterLine.isEmpty() ? EMPTY_LINE : chapterLine;

                if(chapterChunkText.length() + chapterLine.length() + EMPTY_LINE.length() < MAX_CHUNK_LENGTH){
                    if(chapterLine.equals(EMPTY_LINE)) {
                        chapterChunkText.append(chapterLine);
                    } else {
                        chapterChunkText.append(chapterLine).append(EMPTY_LINE);
                    }

                } else {
                    map.put("Chapter " + chapterNumber + "-" + chapterSubNumber, chapterChunkText.toString());
                    chapterChunkText = new StringBuilder();
                    chapterSubNumber++;
                }
            }
            chunks.add(map);
        }

        return chunks;
    }

    @SneakyThrows
    public TreeMap<Integer, List<String>> splitBookToChapters(Path bookPath) {
        TreeMap<Integer, List<String>> chapters = new TreeMap<>();
        List<String> lines = Files.readAllLines(bookPath);
        int chapterNumber = 1;
        int count = 0;
        for (String line : lines) {
            if (line.toLowerCase().startsWith(CHAPTER_SPLITTER) && count == 0) {
                if (lines.get(count + 1).trim().isEmpty()) {
                    List<String> chapterLines = new ArrayList<>();
                    chapterLines.add(line);
                    chapters.put(chapterNumber, chapterLines);
                    chapterNumber++;
                }

            } else if (line.toLowerCase().startsWith(CHAPTER_SPLITTER) && count > 0 && count + 1 < lines.size()) {
                if (lines.get(count - 1).trim().isEmpty() && lines.get(count + 1).trim().isEmpty()) {
                    List<String> chapterLines = new ArrayList<>();
                    chapterLines.add(line);
                    chapters.put(chapterNumber, chapterLines);
                    chapterNumber++;
                }

            } else if (!chapters.isEmpty()) {
                chapters.get(chapters.lastKey()).add(line);
            }

            count++;
        }

        return chapters;
    }
}
