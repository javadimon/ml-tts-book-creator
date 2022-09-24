package com.zubanoff.ml.tts.book.creator.service.tts.stc;

import lombok.Data;

@Data
public class STCConvertResult {

    private String chunkName;
    private boolean isSuccess;
    private long convertDurationInSeconds;
    private String description;
}
