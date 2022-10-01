package com.zubanoff.ml.tts.book.creator;

import com.zubanoff.ml.tts.book.creator.service.tts.stc.STCConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class STCConverterTest {

    @Autowired
    private STCConverter stcConverter;

//    @Disabled
    @Disabled
    @Test
    public void convertTest() throws Exception{
        assertThat(stcConverter, notNullValue());

        stcConverter.createSession();
        stcConverter.checkSession();
        stcConverter.getLanguages();
        stcConverter.getVoices();
        stcConverter.closeSession();
    }

    @Test
    public void callableChunkTest() throws Exception{
        List<String> callables = new ArrayList<>();
        for(int i = 0; i < 121; i++){
            callables.add(Objects.toString(i));
        }
        log.info("callables.size() {}", callables.size());

        for(int fromIndex = 0; fromIndex < callables.size(); fromIndex = fromIndex + 50) {
            int toIndex = (fromIndex + 50) - 1;
            if (toIndex >= callables.size()) {
                toIndex = ((callables.size() - fromIndex) + fromIndex) - 1;
            }
            log.info("Callables fromIndex {}, toIndex {}", fromIndex, toIndex);
        }
    }
}
