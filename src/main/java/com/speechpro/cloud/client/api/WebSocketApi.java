package com.speechpro.cloud.client.api;

import com.neovisionaries.ws.client.*;
import com.speechpro.cloud.client.ApiResponse;
import com.speechpro.cloud.client.model.WebSocketServerConfiguration;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.UUID;


/**
* Created by sadurtinova on 21.06.2018.
*/


public class WebSocketApi {

    private WebSocket ws;

    private UUID transactionId = null;

    private WebSocketAdapter wsAdapter;

    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };

    public WebSocketApi(String webSocketAddress, int timeout, WebSocketAdapter adapter) {
        WebSocketFactory factory = new WebSocketFactory();
        factory.setVerifyHostname(false);
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            factory.setSSLSocketFactory(sslContext.getSocketFactory());
            factory.setVerifyHostname(false);
            //httpClient.setHostnameVerifier((hostname, session) -> true);

            ws = factory.createSocket(webSocketAddress, timeout);
        } catch (Exception e) {
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

