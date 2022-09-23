package com.zubanoff.ml.tts.book.creator.service.tts.stc;

import com.speechpro.cloud.client.ApiClient;
import com.speechpro.cloud.client.api.SessionApi;
import com.speechpro.cloud.client.model.SessionDto;
import com.speechpro.cloud.client.model.StartSessionRequest;
import com.speechpro.cloud.client.model.StatusDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class STCConverter {

    private SessionApi sessionApi;
    private UUID currentSessionId;

    public void init(){
        ApiClient apiClient = new ApiClient();
        sessionApi = new SessionApi(apiClient);
    }

    @SneakyThrows
    public void createSession(){
        StartSessionRequest startSessionRequest = new StartSessionRequest(
                System.getenv("STC_LOGIN"),
                System.getenv("STC_PASSWORD"),
                Long.parseLong(System.getenv("STC_DOMAIN_ID")));
        SessionDto sessionDto = sessionApi.startSession(startSessionRequest);
        currentSessionId = Objects.requireNonNull(sessionDto).getSessionId();
        log.info("Session ID {}", currentSessionId);
    }

    @SneakyThrows
    public void checkSession(){
        StatusDto checkSessionStatus = sessionApi.checkSession(currentSessionId);
        log.info("Check session {}", checkSessionStatus);
    }

    @SneakyThrows
    public void closeSession(){
        sessionApi.closeSession(currentSessionId);
        log.info("Session closed");
    }
}
