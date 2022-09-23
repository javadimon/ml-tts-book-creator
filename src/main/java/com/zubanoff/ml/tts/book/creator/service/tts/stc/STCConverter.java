package com.zubanoff.ml.tts.book.creator.service.tts.stc;

import com.speechpro.cloud.client.ApiClient;
import com.speechpro.cloud.client.ApiException;
import com.speechpro.cloud.client.api.SessionApi;
import com.speechpro.cloud.client.model.SessionDto;
import com.speechpro.cloud.client.model.StartSessionRequest;
import com.speechpro.cloud.client.model.StatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class STCConverter {

    public void createSession(){
        ApiClient apiClient = new ApiClient();
        SessionApi sessionApi = new SessionApi(apiClient);
        StartSessionRequest startSessionRequest = new StartSessionRequest("user", "password", 290L);
        SessionDto sessionDto = null;
        try {
            sessionDto = sessionApi.startSession(startSessionRequest);
        } catch (ApiException e) {
            log.error("FATAL ERROR", e);
            return;
        }
        UUID sessionId = Objects.requireNonNull(sessionDto).getSessionId();

        try{
            StatusDto checkSessionStatus = sessionApi.checkSession(sessionDto.getSessionId());
            checkSessionStatus = sessionApi.checkSession(sessionDto.getSessionId());
            log.info("Check session status {}", checkSessionStatus.getStatus());

        } catch (ApiException e){
            log.error("FATAL ERROR", e);
            return;
        }

        log.info("Session ID {}", sessionId);
    }

    public void closeSession(){
        sessionApi.closeSession(sessionDto.getSessionId());
    }
}
