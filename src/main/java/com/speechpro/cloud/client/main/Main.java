package com.speechpro.cloud.client.main;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.speechpro.cloud.client.ApiClient;
import com.speechpro.cloud.client.ApiException;
import com.speechpro.cloud.client.ApiResponse;
import com.speechpro.cloud.client.api.SessionApi;
import com.speechpro.cloud.client.api.SynthesizeApi;
import com.speechpro.cloud.client.api.WebSocketApi;
import com.speechpro.cloud.client.model.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // for Websocket connection nv-websocket-client is used here
        ApiClient apiClient = new ApiClient();
        SessionApi sessionApi = new SessionApi(apiClient);
        StartSessionRequest startSessionRequest = new StartSessionRequest("user", "password", 290L);
        SessionDto sessionDto = null;
        try {
            sessionDto = sessionApi.startSession(startSessionRequest);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        UUID sessionId = sessionDto.getSessionId();
        SynthesizeApi synthesizeApi = new SynthesizeApi();
        WebSocketSynthesizeRequest webSocketRequest =
                new WebSocketSynthesizeRequest(new WebSocketTextParam("text/plain"), "Carol", "audio/wav");
        ApiResponse<WebSocketServerConfiguration> webSocketConfiguration = null;
        try {
            webSocketConfiguration = synthesizeApi.webSocketStreamWithHttpInfo(sessionId, webSocketRequest);
        } catch (ApiException e) {
            e.printStackTrace();
        }
// you should implement methods for different events
        WebSocketApi webSocketApi = new WebSocketApi(webSocketConfiguration.getData().getUrl(), 5000,
                new WebSocketAdapter() {
                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                        System.out.println("Connected");
                    }
                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                        System.out.println(cause.getMessage());
                        System.out.println("Error!");
                    }
                    @Override
                    public void onBinaryMessage(WebSocket websocket, byte[] binary){
                        System.out.println("Received: " + binary.length);
                    }
                });
        webSocketApi.connect();
        webSocketApi.sendText("One two three");
    }
}
