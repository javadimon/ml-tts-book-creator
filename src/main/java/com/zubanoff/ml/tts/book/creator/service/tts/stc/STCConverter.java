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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component(value = "STCConverter")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class STCConverter {

    private SessionApi sessionApi;
    private UUID currentSessionId;
    private AudioFormat audioFormat;

    @PostConstruct
    public void init() {
        ApiClient apiClient = new ApiClient();
        sessionApi = new SessionApi(apiClient);

        boolean bigEndian = false; // младший байт первый
        boolean signed = true;
        int channels = 1;
        int sampleSizeInBits = 16;
        float sampleRate = 22050;
        audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
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
    public Callable<STCConvertResult> convert(String chunkName, String text) {
        Callable<STCConvertResult> callable = () -> {

            STCConvertResult result = new STCConvertResult();
            result.setChunkName(chunkName);
            result.setDescription("Ok");

            long startTime = System.currentTimeMillis();
            try{
                ByteArrayOutputStream audioBytes = new ByteArrayOutputStream();
                UUID transactionId = UUID.randomUUID();
                SynthesizeApi synthesizeApi = new SynthesizeApi();
                WebSocketSynthesizeRequest webSocketRequest =
                        new WebSocketSynthesizeRequest(new WebSocketTextParam("text/plain"), "Petr_n", "audio/wav");
                ApiResponse<WebSocketServerConfiguration> webSocketConfiguration = synthesizeApi.webSocketStreamWithHttpInfo(currentSessionId, webSocketRequest);
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
                                log.info("Connected for {}", chunkName);
                                isConnect.set(true);
                            }

                            @Override
                            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                                log.error("FATAL ERROR WEBSOCKET", cause);
                            }

                            @Override
                            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                                audioBytes.write(binary);
                                prevCurrentTime.set(currentTime.get());
//                        log.info("Message text onBinaryMessage {}", binary.length);
                            }
                        });

                webSocketApi.connect();

                while(!isConnect.get()){
                    Thread.sleep(1);
                }

                webSocketApi.sendText(text);
                log.info("Text for chunk {} sent", chunkName);

                while (isWait.get()){
                    Thread.sleep(10);
                    currentTime.set(System.nanoTime());
                    if(prevCurrentTime.get() != 0 && (currentTime.get() - prevCurrentTime.get()) > 1_000_000_000L){
                        isWait.set(false);
                    }
                }

                AudioInputStream audioInputStream = new AudioInputStream(
                        new ByteArrayInputStream(audioBytes.toByteArray()),
                        audioFormat,
                        audioBytes.toByteArray().length);
                Path destination = Paths.get(System.getProperty("user.dir"), "books", "out", "mp3", chunkName + ".wav");
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, destination.toFile());
                convertToMp3(chunkName);

                log.info("File {} created", chunkName + ".np3");
                closeSession();

                result.setSuccess(true);
                result.setConvertDurationInSeconds((System.currentTimeMillis() - startTime) / 1000);
                return result;

            } catch (Exception ex){
                log.error("FATAL TTS ERROR FOR {}", chunkName, ex);
                closeSession();

                result.setSuccess(false);
                result.setDescription(ex.getMessage());
                result.setConvertDurationInSeconds((System.currentTimeMillis() - startTime) / 1000);
                return result;
            }
        };

        return callable;
    }

    @SneakyThrows
    private void convertToMp3(String chunkName){

        Path ffmpegPath = Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin", "ffmpeg.exe");
        Path wavPath = Paths.get(System.getProperty("user.dir"), "books", "out", "mp3", chunkName + ".wav");
        Path mp3Path = Paths.get(System.getProperty("user.dir"), "books", "out", "mp3", chunkName + ".mp3");

        log.info("Try convert {} to mp3", chunkName);
        String cmd = "{ffmpegPath} -y -i \"{wavPath}\" -acodec libmp3lame -b:a 128k \"{mp3Path}\"";
        cmd = cmd.replace("{ffmpegPath}", ffmpegPath.toString())
                .replace("{wavPath}", wavPath.toString())
                .replace("{mp3Path}", mp3Path.toString());
        log.info("DEBUG ffmpeg cmd {}", cmd);

        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        log.info("Converted {} success!", chunkName);

        boolean isDeleted = wavPath.toFile().delete();
        log.info("Result for deleted {} is {}", wavPath, isDeleted);
    }
}
