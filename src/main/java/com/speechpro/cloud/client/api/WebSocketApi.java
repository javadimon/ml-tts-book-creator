package com.speechpro.cloud.client.api;

import com.neovisionaries.ws.client.*;
import com.speechpro.cloud.client.ApiResponse;
import com.speechpro.cloud.client.model.WebSocketServerConfiguration;

import java.io.IOException;
import java.util.UUID;


/**
* Created by sadurtinova on 21.06.2018.
*/


public class WebSocketApi {

    private WebSocket ws;

    private UUID transactionId = null;

    private WebSocketAdapter wsAdapter;

    public WebSocketApi(String webSocketAddress, int timeout, WebSocketAdapter adapter) {
        WebSocketFactory factory = new WebSocketFactory();
        factory.setVerifyHostname(false);
        try {
            ws = factory.createSocket(webSocketAddress, timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        wsAdapter = adapter;
        ws.addListener(wsAdapter);
    }

    public WebSocketApi(ApiResponse<WebSocketServerConfiguration> apiResponse, int timeout, WebSocketAdapter adapter){
        WebSocketFactory factory = new WebSocketFactory();
        wsAdapter = adapter;
        factory.setVerifyHostname(false);
        try {
            ws = factory.createSocket(apiResponse.getData().getUrl(), timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ws.addListener(wsAdapter);
        transactionId = UUID.fromString(apiResponse.getHeaders().get("X-Transaction-Id").get(0));
    }

    public void connect() {
        try {
            ws.connect();
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    public void sendBytes(byte[] bytes){
        ws.sendBinary(bytes);
    }

    public void sendText(String text){
        ws.sendText(text);
    }

    public UUID getTransactionId(){
        return transactionId;
    }
}

