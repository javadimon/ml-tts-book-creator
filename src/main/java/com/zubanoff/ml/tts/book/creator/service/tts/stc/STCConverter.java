package com.zubanoff.ml.tts.book.creator.service.tts.stc;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.speechpro.cloud.client.ApiClient;
import com.speechpro.cloud.client.ApiResponse;
import com.speechpro.cloud.client.api.SessionApi;
import com.speechpro.cloud.client.api.SynthesizeApi;
import com.speechpro.cloud.client.api.WebSocketApi;
import com.speechpro.cloud.client.model.SessionDto;
import com.speechpro.cloud.client.model.StartSessionRequest;
import com.speechpro.cloud.client.model.StatusDto;
import com.speechpro.cloud.client.model.SynthesizeLanguage;
import com.speechpro.cloud.client.model.SynthesizeVoiceType;
import com.speechpro.cloud.client.model.WebSocketServerConfiguration;
import com.speechpro.cloud.client.model.WebSocketSynthesizeRequest;
import com.speechpro.cloud.client.model.WebSocketTextParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class STCConverter {

    private SessionApi sessionApi;
    private UUID currentSessionId;
    private ApiResponse<WebSocketServerConfiguration> webSocketConfiguration;

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient();
        sessionApi = new SessionApi(apiClient);
    }

    @SneakyThrows
    public void createSession() {
        StartSessionRequest startSessionRequest = new StartSessionRequest(
                System.getenv("STC_LOGIN"),
                System.getenv("STC_PASSWORD"),
                Long.parseLong(System.getenv("STC_DOMAIN_ID")));
        SessionDto sessionDto = sessionApi.startSession(startSessionRequest);
        currentSessionId = Objects.requireNonNull(sessionDto).getSessionId();
        log.info("Session ID {}", currentSessionId);
    }

    @SneakyThrows
    public void checkSession() {
        StatusDto checkSessionStatus = sessionApi.checkSession(currentSessionId);
        log.info("Check session {}", checkSessionStatus);
    }

    @SneakyThrows
    public void closeSession() {
        sessionApi.closeSession(currentSessionId);
        log.info("Session closed");
    }

    @SneakyThrows
    public void getLanguages() {
        SynthesizeApi synthesizeClient = new SynthesizeApi();
        List<SynthesizeLanguage> languages = synthesizeClient.languageVoicesSupport(currentSessionId.toString());
        log.info("Languages {}", languages);
    }

    @SneakyThrows
    public void getVoices() {
        SynthesizeApi synthesizeClient = new SynthesizeApi();
        List<SynthesizeVoiceType> voices = synthesizeClient.voices(currentSessionId, "Russian");
        log.info("Voices {}", voices);
        // Vladimir_n
    }

    @SneakyThrows
    public void convert(String chunkName, String text) {
        UUID transactionId = UUID.randomUUID();
        SynthesizeApi synthesizeApi = new SynthesizeApi();
        WebSocketSynthesizeRequest webSocketRequest =
                new WebSocketSynthesizeRequest(new WebSocketTextParam("text/plain"), "Vladimir_n", "audio/wav");
        webSocketConfiguration = synthesizeApi.webSocketStreamWithHttpInfo(currentSessionId, webSocketRequest);
        webSocketConfiguration.getHeaders().put("X-Transaction-Id", List.of(transactionId.toString()));

        AtomicBoolean isConnect = new AtomicBoolean(false);
        AtomicBoolean isWait = new AtomicBoolean(true);
        AtomicReference<Long> currentTime = new AtomicReference<>();
        currentTime.set(0L);
        AtomicReference<Long> prevCurrentTime = new AtomicReference<>();
        prevCurrentTime.set(0L);

        WebSocketApi webSocketApi = new WebSocketApi(Objects.requireNonNull(webSocketConfiguration).getData().getUrl(), 5000,
                new WebSocketAdapter() {
                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                        log.info("Connected");
                        isConnect.set(true);
                    }

                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                        log.error("FATAL ERROR WEBSOCKET", cause);
                    }

                    @Override
                    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                        prevCurrentTime.set(currentTime.get());
                        log.info("Message text onBinaryMessage {}", binary.length);
                    }
                });

        webSocketApi.connect();

        while(!isConnect.get()){
            Thread.sleep(1);
        }

        webSocketApi.sendText("Волков вздохнул, съехал на обочину и остановил коня, жестом дал знак оруженосцу с его " +
                "штандартом и слугам, Ёгану и Сычу, ехать дальше, сам стал пропускать колону вперёд.");
        log.info("Text sent");

        while (isWait.get()){
            Thread.sleep(10);
            currentTime.set(System.nanoTime());
            if(prevCurrentTime.get() != 0 && (currentTime.get() - prevCurrentTime.get()) > 100000000L){
                isWait.set(false);
            }
        }
        log.info("Finish");
    }
}
