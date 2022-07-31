package com.zubanoff.ml.tts.book.creator;

import com.zubanoff.ml.tts.book.creator.service.converter.Converter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConverterTest {

    @Autowired
    public Converter converter;

    @Test
    @SneakyThrows
    public void convertTest(){
        String text = """
                Если подъезжать к Ланну с юга, то с холмов его видно из далека. А слышно еще дальше. Колокола Ланна известны на весь мир, что чтит Истинного Бога и Мать Церковь. Издали город кажется огромным и прекрасным. Чистым и белым.""";
        converter.convert(text);

        Thread.sleep(10000);
    }
}
