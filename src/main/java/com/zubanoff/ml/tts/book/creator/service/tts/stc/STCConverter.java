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

@Component
@Slf4j
public class STCConverter {

    private SessionApi sessionApi;
    private UUID currentSessionId;

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
        // for Websocket connection nv-websocket-client is used here
        SynthesizeApi synthesizeApi = new SynthesizeApi();
        WebSocketSynthesizeRequest webSocketRequest =
                new WebSocketSynthesizeRequest(new WebSocketTextParam("text/plain"), "Vladimir_n", "audio/wav");
        ApiResponse<WebSocketServerConfiguration> webSocketConfiguration = synthesizeApi.webSocketStreamWithHttpInfo(currentSessionId, webSocketRequest);

        WebSocketApi webSocketApi = new WebSocketApi(Objects.requireNonNull(webSocketConfiguration).getData().getUrl(), 5000,
                new WebSocketAdapter() {
                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                        log.info("Connected");
                    }

                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                        log.error("FATAL ERROR WEBSOCKET", cause);
                    }

                    @Override
                    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                        log.info("Message text onBinaryMessage {}", binary.length);
                    }
                });
        webSocketApi.connect();

        Thread.sleep(5000);
        webSocketApi.sendText("Волков вздохнул, съехал на обочину и остановил коня, жестом дал знак оруженосцу с его " +
                "штандартом и слугам, Ёгану и Сычу, ехать дальше, сам стал пропускать колону вперёд.");
        log.info("Text sent");
    }

    @SneakyThrows
    public void closeWebSocket() {
        SynthesizeApi synthesizeApi = new SynthesizeApi();
        WebSocketSynthesizeRequest webSocketRequest =
                new WebSocketSynthesizeRequest(new WebSocketTextParam("text/plain"), "Vladimir_n", "audio/wav");
        ApiResponse<WebSocketServerConfiguration> webSocketConfiguration = null;
        webSocketConfiguration = synthesizeApi.webSocketStreamWithHttpInfo(currentSessionId, webSocketRequest);
        String transactionId = Objects.requireNonNull(webSocketConfiguration).getHeaders().get("X-Transaction-Id").get(0);
        synthesizeApi.closeWebSocketStream(currentSessionId, UUID.fromString(transactionId));
    }
}
