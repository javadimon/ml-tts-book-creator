package com.zubanoff.ml.tts.book.creator.server.dto;

import lombok.Getter;
import lombok.Setter;


import java.util.UUID;

@Getter
@Setter
public class BookCreateRequestDto {
    private UUID bookId;
    private int fromChapter;
    private int toChapter;
}
