package com.aspiralimited.oddsmarket.client.wsevents;

import com.aspiralimited.oddsmarket.client.wsevents.exception.MessageException;
import com.aspiralimited.oddsmarket.client.wsevents.message.PingWebSocketMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.PongWebSocketMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.WebSocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class OddsmarketClient {

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(0);
    public static final Duration DEFAULT_PING_INTERVAL = Duration.ofSeconds(30);
    public static final Duration DEFAULT_PONG_TIMEOUT = Duration.ofSeconds(10);

    private static final int CLOSE_CODE_PONG_TIMEOUT = 5002;
    private static final String CLOSE_REASON_PONG_TIMEOUT = "Pong timeout";

    @Getter
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final List<OddsmarketWebSocketListener> webSocketListeners = new CopyOnWriteArrayList<>();

    private final Options options;

    private WebSocket activeWebsocket;

    public synchronized void connect() throws IOException, WebSocketException, URISyntaxException {
        if (activeWebsocket != null) {
            activeWebsocket.disconnect();
        }
        activeWebsocket = new WebSocketFactory()
                .setConnectionTimeout((int) options.connectTimeout.toMillis())
                .createSocket(createUri())
                .addListener(new WebSocketListenerImpl())
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect();
    }

    private URI createUri() throws URISyntaxException {
        URI uri = URI.create(options.websocketUrl);

        String query = uri.getQuery();
        if (query == null) {
            query = "apiKey=" + options.apiKey;
        } else {
            query += "&apiKey=" + options.apiKey;
        }

        if (!options.eventIds.isEmpty()) {
            query += "&eventIds=" + options.eventIds.stream().map(Object::toString).collect(Collectors.joining(","));
        } else if (!options.sportIds.isEmpty()) {
            query += "&sportIds=" + options.sportIds.stream().map(Object::toString).collect(Collectors.joining(","));
        }

        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                uri.getPath(), query, uri.getFragment());
    }

    public synchronized void disconnect() {
        if (activeWebsocket != null) {
            activeWebsocket.disconnect();
        }
    }

    public synchronized void send(WebSocketMessage message) throws MessageException {
        if (activeWebsocket == null) {
            throw new IllegalStateException("WebSocket is not connected");
        }
        send(activeWebsocket, message);
    }

    private void send(WebSocket webSocket, WebSocketMessage message) throws MessageException {
        try {
            webSocket.sendText(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new MessageException("Error serializing message", e);
        }
    }

    public void addWebSocketListener(OddsmarketWebSocketListener listener) {
        webSocketListeners.add(listener);
    }

    public void removeWebSocketListener(OddsmarketWebSocketListener listener) {
        webSocketListeners.remove(listener);
    }

    private class WebSocketListenerImpl extends WebSocketAdapter {

        private final Map<WebSocket, PingPongHandler> pingPongHandlerMap = new ConcurrentHashMap<>();

        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) {
            log.debug("WebSocket state changed: {}", newState);
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            log.info("WebSocket connected");

            for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                webSocketListener.onConnected();
            }

            PingPongHandler pingPongHandler = new PingPongHandler(websocket);
            pingPongHandlerMap.put(websocket, pingPongHandler);
            pingPongHandler.start();
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                   WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {

            PingPongHandler pingPongHandler = pingPongHandlerMap.remove(websocket);
            if (pingPongHandler != null) {
                pingPongHandler.stop();
            }

            int closeCode;
            String closeReason;
            if (closedByServer) {
                closeCode = serverCloseFrame.getCloseCode();
                closeReason = serverCloseFrame.getCloseReason();
            } else {
                closeCode = clientCloseFrame.getCloseCode();
                closeReason = clientCloseFrame.getCloseReason();
            }

            for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                webSocketListener.onDisconnected(closeCode, closeReason, closedByServer);
            }

            if (closedByServer || clientCloseFrame.getCloseCode() == CLOSE_CODE_PONG_TIMEOUT) {
                log.error("WebSocket disconnected: [code={}, reason={}]", closeCode, closeReason);
                if (options.autoReconnect) {
                    log.warn("Reconnection to WebSocket");
                    for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                        webSocketListener.onReconnecting();
                    }
                    activeWebsocket = websocket.recreate().connect();
                    for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                        webSocketListener.onReconnected();
                    }
                }
            } else {
                log.info("WebSocket disconnected: [code={}, reason={}]", closeCode, closeReason);
            }
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            WebSocketMessage message;
            try {
                message = objectMapper.readValue(text, WebSocketMessage.class);
            } catch (InvalidTypeIdException e) {
                log.debug("Skipping unknown message type: {}", text);
                return;
            }
            PingPongHandler pingPongHandler = pingPongHandlerMap.get(websocket);
            if (pingPongHandler != null) {
                pingPongHandler.onWebSocketMessage(message);
            }
            for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                webSocketListener.onWebSocketMessage(message);
            }
            for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                webSocketListener.onTextMessage(text);
            }
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) {
            log.error("WebSocket error", cause);
            for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                webSocketListener.onError(cause);
            }
        }

        @Override
        public void handleCallbackError(WebSocket websocket, Throwable cause) {
            log.error("WebSocket uncaught exception", cause);
            for (OddsmarketWebSocketListener webSocketListener : webSocketListeners) {
                webSocketListener.handleUncaughtException(cause);
            }
        }
    }

    @RequiredArgsConstructor
    private class PingPongHandler {

        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final BlockingQueue<String> pongQueue = new LinkedBlockingQueue<>(10);

        private final WebSocket websocket;

        public void start() {
            scheduler.scheduleWithFixedDelay(this::run, options.pingInterval.toMillis(),
                    options.pingInterval.toMillis(), TimeUnit.MILLISECONDS);
        }

        public void stop() {
            scheduler.shutdownNow();
        }

        private void run() {
            String payload = String.valueOf(System.currentTimeMillis());

            try {
                log.debug("Sending ping message: [payload={}]", payload);
                send(websocket, PingWebSocketMessage.builder().payload(payload).build());
            } catch (MessageException e) {
                log.error("Error sending ping message", e);
                return;
            }

            if (options.pingInterval.isZero()) {
                return;
            }

            long deadline = System.currentTimeMillis() + options.pongTimeout.toMillis();

            while (!Thread.currentThread().isInterrupted()) {
                long timeout = deadline - System.currentTimeMillis();
                if (timeout <= 0) {
                    onPongTimeout(payload);
                    return;
                }
                try {
                    String receivedPayload = pongQueue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (receivedPayload == null) {
                        onPongTimeout(payload);
                        return;
                    } else if (payload.equals(receivedPayload)) {
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        public void onWebSocketMessage(WebSocketMessage message) {
            if (message instanceof PongWebSocketMessage) {
                String payload = ((PongWebSocketMessage) message).getPayload();
                log.debug("Received pong message: [payload={}]", payload);
                if (!options.pingInterval.isZero()) {
                    pongQueue.add(payload);
                }
            }
        }

        private void onPongTimeout(String payload) {
            log.debug("Pong timeout: [payload={}]", payload);
            websocket.sendClose(CLOSE_CODE_PONG_TIMEOUT, CLOSE_REASON_PONG_TIMEOUT);
        }
    }

    @Value
    @Builder
    public static class Options {

        @NonNull
        String websocketUrl;

        @NonNull
        String apiKey;

        @Singular
        List<Long> eventIds;

        @Singular
        List<Short> sportIds;

        @Builder.Default
        boolean autoReconnect = true;

        @NonNull
        @Builder.Default
        Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        @NonNull
        @Builder.Default
        Duration pingInterval = DEFAULT_PING_INTERVAL;

        @NonNull
        @Builder.Default
        Duration pongTimeout = DEFAULT_PONG_TIMEOUT;
    }
}
