package com.zubanoff.ml.tts.book.creator.server.controller;

import com.zubanoff.ml.tts.book.creator.server.dto.BookCreateRequestDto;
import com.zubanoff.ml.tts.book.creator.service.BookCreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book")
public class BookCreatorController {

    private final BookCreatorService bookCreatorService;

    @PostMapping("/create")
    public void createBook(BookCreateRequestDto bookCreateRequestDto) {
        bookCreatorService.createBook(bookCreateRequestDto);
    }
}
