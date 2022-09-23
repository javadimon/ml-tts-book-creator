package com.zubanoff.ml.tts.book.creator.service.tts.yandex;

import lombok.Getter;
import lombok.Setter;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class YandexConverterRequest {
    private String text = "";
    private String lang = "ru-RU";
    private String speed = "1.2";
    private String voice = "zahar";
    private String emotion = "good";
    private String format = "mp3";
    private String folderId = "b1gs24si0ae1hf7ouuho";

    public String getRequest() {
        return "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                "&lang=" + lang +
                "&speed=" + speed +
                "&voice=" + voice +
                "&emotion=" + emotion +
                "&format=" + format +
                "&folderId=" + folderId;
    }
}
