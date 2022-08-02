package com.zubanoff.ml.tts.book.creator.service;

import com.zubanoff.ml.tts.book.creator.dao.BookRepository;
import com.zubanoff.ml.tts.book.creator.model.BookEntity;
import com.zubanoff.ml.tts.book.creator.server.dto.BookCreateRequestDto;
import com.zubanoff.ml.tts.book.creator.service.converter.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookCreatorService {

    private final Converter converter;
    private final BookRepository bookRepository;
    private static final String CHAPTER_SPLITTER = "глава";
    private static final String EMPTY_LINE = "\n";
    private static final int MAX_CHUNK_LENGTH = 5000;
    private static final int CONVERT_REQUEST_DELAY_MS = 50;

    @SneakyThrows
    public void createBook(BookCreateRequestDto bookCreateRequestDto) {
        BookEntity bookEntity = bookRepository.findById(bookCreateRequestDto.getBookId()).orElseThrow();
        Path bookPath = Paths.get(System.getProperty("user.dir"), "books", "source", "txt", bookEntity.getFileName());
        TreeMap<Integer, List<String>> chapters = splitBookToChapters(bookPath);
        List<TreeMap<String, String>> chunks = splitChaptersToChunks(chapters);
        for(TreeMap<String, String> chunk : chunks){
            for(Map.Entry<String, String> entry : chunk.entrySet()){
                if(entry.getKey().startsWith("Chapter 009")){
                    log.info("Try convert chapter {}, length {}", entry.getKey(), entry.getValue().length());
                    Runnable runnable = () -> {
                        converter.convert(entry.getKey(), entry.getValue());
                    };
                    Executors.newSingleThreadExecutor().execute(runnable);

                    Thread.sleep(CONVERT_REQUEST_DELAY_MS);
                }
            }
        }

        // TODO MERGE MP3 CHUNKS TO MP3 CHAPTERS
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
                    map.put("Chapter " + formatChapterNumber(chapterNumber) + "-" + formatChapterNumber(chapterSubNumber), chapterChunkText.toString());
                    chapterChunkText = new StringBuilder();
                    chapterSubNumber++;
                }
            }

            if(chapterChunkText.length() > 0) {
                map.put("Chapter " + formatChapterNumber(chapterNumber) + "-" + formatChapterNumber(chapterSubNumber), chapterChunkText.toString());
            }
            chunks.add(map);
        }

        return chunks;
    }

    private String formatChapterNumber(int chapterNumber) {
        if(chapterNumber < 100) {
            return chapterNumber < 10 ? "00" + chapterNumber : "0" + String.valueOf(chapterNumber);
        } else {
            return String.valueOf(chapterNumber);
        }
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
