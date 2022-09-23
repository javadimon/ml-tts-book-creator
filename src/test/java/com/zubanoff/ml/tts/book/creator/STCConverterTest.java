package com.zubanoff.ml.tts.book.creator;

import com.zubanoff.ml.tts.book.creator.service.tts.stc.STCConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class STCConverterTest {

    @Autowired
    private STCConverter stcConverter;

//    @Disabled
    @Test
    public void convertTest() throws Exception{
        assertThat(stcConverter, notNullValue());

        stcConverter.createSession();
        stcConverter.checkSession();
        stcConverter.closeSession();
    }
}
