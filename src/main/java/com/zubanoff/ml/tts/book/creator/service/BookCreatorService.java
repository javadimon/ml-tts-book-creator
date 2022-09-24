package com.zubanoff.ml.tts.book.creator.service;

import com.zubanoff.ml.tts.book.creator.dao.BookRepository;
import com.zubanoff.ml.tts.book.creator.model.BookEntity;
import com.zubanoff.ml.tts.book.creator.server.dto.BookCreateRequestDto;
import com.zubanoff.ml.tts.book.creator.service.tts.stc.STCConverter;
import com.zubanoff.ml.tts.book.creator.service.tts.yandex.YandexConverter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookCreatorService {

    private final YandexConverter yandexConverter;
    private final ApplicationContext context;
    private final BookRepository bookRepository;
    private static final String CHAPTER_SPLITTER = "глава";
    private static final String EMPTY_LINE = "\n";
    private static final int MAX_CHUNK_LENGTH = 5000;
    private static final int CONVERT_REQUEST_DELAY_MS = 50;
    private static final double COST_PER_SYMBOL = 0.00132;

    @SneakyThrows
    public void createBook(BookCreateRequestDto bookCreateRequestDto) {

        BookEntity bookEntity = bookRepository.findById(bookCreateRequestDto.getBookId()).orElseThrow();
        Path bookPath = Paths.get(System.getProperty("user.dir"), "books", "source", "txt", bookEntity.getFileName());
        TreeMap<Integer, List<String>> chapters = splitBookToChapters(bookPath);
        List<TreeMap<String, String>> chunks = splitChaptersToChunks(chapters);
        int totalSymbolsCount = 0;
        for (TreeMap<String, String> chunk : chunks) {

            ExecutorService executor = Executors.newFixedThreadPool(chunks.size());
            List<Callable<Boolean>> callables = new ArrayList<>();
            for (Map.Entry<String, String> entry : chunk.entrySet()) {
                totalSymbolsCount = totalSymbolsCount + entry.getValue().length();

                if (isChapterToConvert(bookCreateRequestDto, entry.getKey())) {
                    log.info("Key {}, Value length {}", entry.getKey(), entry.getValue().length());
                    STCConverter stcConverter = context.getBean(STCConverter.class);
                    stcConverter.createSession();
                    callables.add(stcConverter.convert(entry.getKey(), entry.getValue()));
                }

                if(callables.size() > 49){
                    List<Future<Boolean>> results = executor.invokeAll(callables);
                    executor.shutdown();

                    for(int i = 0; i < results.size(); i++){
                        log.info("Result for Future number {} is success {}", i, results.get(i).get());
                    }

                    executor = Executors.newFixedThreadPool(chunks.size());
                    callables = new ArrayList<>();
                }
            }

            List<Future<Boolean>> results = executor.invokeAll(callables);
            executor.shutdown();

            for(int i = 0; i < results.size(); i++){
                log.info("Result for Future number {} is success {}", i, results.get(i).get());
            }
        }
        log.info("Total symbols count {}, Price {}", totalSymbolsCount, totalSymbolsCount * COST_PER_SYMBOL);

        makeZipFile(bookEntity);
    }

    private boolean isChapterToConvert(BookCreateRequestDto bookCreateRequestDto, String chapterName) {
        for (int chapterNumber = bookCreateRequestDto.getFromChapter(); chapterNumber < bookCreateRequestDto.getToChapter(); chapterNumber++) {
            String sChapterNumber = "Chapter " + formatChapterNumber(chapterNumber);
            if (chapterName.startsWith(sChapterNumber)) {
                return true;
            }
        }

        return false;
    }

    @SneakyThrows
    private void makeZipFile(BookEntity bookEntity) {
        Path srcFilesPath = Paths.get(System.getProperty("user.dir"), "books", "out", "mp3");
        File[] srcFiles = srcFilesPath.toFile().listFiles();

        Path zipPath = Paths.get(System.getProperty("user.dir"), "books", "out", "zip", bookEntity.getName() + ".zip");
        FileOutputStream fos = new FileOutputStream(zipPath.toFile());
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File srcFile : Objects.requireNonNull(srcFiles)) {
            FileInputStream fis = new FileInputStream(srcFile);
            ZipEntry zipEntry = new ZipEntry(srcFile.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();

        log.info("ZIP file created!");
    }

    public List<TreeMap<String, String>> splitChaptersToChunks(TreeMap<Integer, List<String>> chapters) {
        List<TreeMap<String, String>> chunks = new ArrayList<>();

        for (Map.Entry<Integer, List<String>> chapter : chapters.entrySet()) {
            int chapterNumber = chapter.getKey();
            int chapterSubNumber = 1;

            TreeMap<String, String> map = new TreeMap<>();
            StringBuilder chapterChunkText = new StringBuilder();
            for (String chapterLine : chapter.getValue()) {

                chapterLine = chapterLine.isEmpty() ? EMPTY_LINE : chapterLine;

                if (chapterChunkText.length() + chapterLine.length() + EMPTY_LINE.length() < MAX_CHUNK_LENGTH) {
                    if (chapterLine.equals(EMPTY_LINE)) {
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

            if (chapterChunkText.length() > 0) {
                map.put("Chapter " + formatChapterNumber(chapterNumber) + "-" + formatChapterNumber(chapterSubNumber), chapterChunkText.toString());
            }
            chunks.add(map);
        }

        return chunks;
    }

    private String formatChapterNumber(int chapterNumber) {
        if (chapterNumber < 100) {
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

    public String getBookChapters(String bookId) {
        BookEntity bookEntity = bookRepository.findById(UUID.fromString(bookId)).orElseThrow();
        Path bookPath = Paths.get(System.getProperty("user.dir"), "books", "source", "txt", bookEntity.getFileName());
        TreeMap<Integer, List<String>> chapters = splitBookToChapters(bookPath);
        return chapters.lastKey().toString();
    }
}
