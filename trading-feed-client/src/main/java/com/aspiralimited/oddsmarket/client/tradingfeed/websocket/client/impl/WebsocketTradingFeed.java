package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.impl;


import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedDispatcher;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedNewSessionParams;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeed;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionResult;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.*;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebsocketTradingFeed implements TradingFeed {
    private final static String NEW_SESSION_URI = "/trading/new_ws_session";
    private final static String RESUME_SESSION_URI = "/trading/resume_ws_session";

    private volatile TradingFeedState currentTradingFeedState = TradingFeedState.UNHEALTHY;
    private volatile String sessionId;
    private final String host;
    private WebSocket currentSessionWebSocket;
    private final String apiKey;
    private final short tradingFeedId;
    private final TradingFeedNewSessionParams tradingFeedNewSessionParams;
    @Setter
    private TradingFeedListener tradingFeedListener;
    @Setter
    private TradingFeedDispatcher tradingFeedDispatcher;
    private volatile long lastConsumedMessageId;

    public WebsocketTradingFeed(String host, String apiKey, short tradingFeedId, TradingFeedNewSessionParams tradingFeedNewSessionParams) {
        this.host = host;
        this.apiKey = apiKey;
        this.tradingFeedId = tradingFeedId;
        this.tradingFeedNewSessionParams = tradingFeedNewSessionParams;

    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public CompletableFuture<TradingFeedConnectionResult> establishNewSession() throws IOException, InterruptedException, WebSocketException {
        String websocketUrl = constructNewSessionUrl(host, apiKey, tradingFeedId, tradingFeedNewSessionParams);

        CompletableFuture<TradingFeedConnectionResult> resultFuture = new CompletableFuture<>();
        currentSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
        currentSessionWebSocket.addListener(new WebSocketAdapter() {
            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                currentTradingFeedState = TradingFeedState.HEALTHY;
                resultFuture.complete(TradingFeedConnectionResult.success());
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws InterruptedException, IOException, WebSocketException, ExecutionException, TimeoutException {
                currentTradingFeedState = TradingFeedState.UNHEALTHY;
                if (isResumableSession()) {
                    if (tradingFeedListener != null) {
                        tradingFeedListener.onDisconnected(WebsocketTradingFeed.this);
                    }
                }
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] data) throws InvalidProtocolBufferException {
                OddsmarketTradingDto.ServerMessage serverMessage = OddsmarketTradingDto.ServerMessage.parseFrom(data);
                lastConsumedMessageId = serverMessage.getMessageId();
                if (serverMessage.getPayloadCase() == OddsmarketTradingDto.ServerMessage.PayloadCase.SESSIONSTART) {
                    OddsmarketTradingDto.SessionStart sessionStart = serverMessage.getSessionStart();
                    sessionId = sessionStart.getSessionId();
                }
                if (tradingFeedDispatcher.isTradingFeedActive(WebsocketTradingFeed.this) && tradingFeedListener != null) {
                    tradingFeedListener.onServerMessage(serverMessage);
                }
            }

            @Override
            public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) {
                currentTradingFeedState = TradingFeedState.UNHEALTHY;
                int statusCode = frame.getCloseCode();
                TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode = TradingFeedConnectionStatusCode.detectTradingFeedConnectionErrorCodeByErrorCode(statusCode);
                resultFuture.complete(TradingFeedConnectionResult.error(tradingFeedConnectionStatusCode, frame.getCloseReason()));
                if (tradingFeedListener != null) {
                    tradingFeedListener.onConnectError(tradingFeedConnectionStatusCode);
                }
            }
        });
        try {
            currentSessionWebSocket.connect();
        } catch (Exception e) {
            resultFuture.complete(TradingFeedConnectionResult.error(TradingFeedConnectionStatusCode.CONNECTION_FAILED, e.getMessage()));
        }
        return resultFuture;
    }


    private static String constructNewSessionUrl(String host, String apiKey,
                                                 short tradingFeedId, TradingFeedNewSessionParams tradingFeedNewSessionParams) {
        return host + NEW_SESSION_URI + "?apiKey=" + apiKey + "&tradingFeedId=" + tradingFeedId + (tradingFeedNewSessionParams != null ? tradingFeedNewSessionParams.toQueryString() : "");
    }

    private boolean isResumableSession() {
        return sessionId != null && tradingFeedNewSessionParams.getResumeBufferLimitSeconds() != null && tradingFeedNewSessionParams.getResumeBufferLimitSeconds() > 0;
    }

    @Override
    public CompletableFuture<TradingFeedConnectionStatusCode> resumeSession() throws IOException {
        if (sessionId == null) {
            return CompletableFuture.completedFuture(TradingFeedConnectionStatusCode.BAD_REQUEST);
        }
        String websocketUrl = constructResumeSessionUrl(lastConsumedMessageId);
        currentSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
        CompletableFuture<TradingFeedConnectionStatusCode> resultFuture = new CompletableFuture<>();
        currentSessionWebSocket.addListener(new WebSocketAdapter() {

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                currentTradingFeedState = TradingFeedState.HEALTHY;
                resultFuture.complete(TradingFeedConnectionStatusCode.SUCCESS);
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws IOException, InterruptedException, WebSocketException, ExecutionException, TimeoutException {
                currentTradingFeedState = TradingFeedState.UNHEALTHY;
                if (tradingFeedListener != null) {
                    tradingFeedListener.onDisconnected(WebsocketTradingFeed.this);
                }
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] data) throws Exception {
                if (tradingFeedListener != null) {
                    OddsmarketTradingDto.ServerMessage serverMessage = OddsmarketTradingDto.ServerMessage.parseFrom(data);
                    tradingFeedListener.onServerMessage(serverMessage);
                }
            }

            @Override
            public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) {
                currentTradingFeedState = TradingFeedState.UNHEALTHY;
                int statusCode = frame.getCloseCode();
                TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode = TradingFeedConnectionStatusCode.detectTradingFeedConnectionErrorCodeByErrorCode(statusCode);
                resultFuture.complete(tradingFeedConnectionStatusCode);
                if (tradingFeedListener != null) {
                    tradingFeedListener.onConnectError(tradingFeedConnectionStatusCode);
                }
            }
        });
        try {
            currentSessionWebSocket.connect();
        } catch (Exception e) {
            resultFuture.complete(TradingFeedConnectionStatusCode.CONNECTION_FAILED); // <-- или что-то подходящее
        }
        return resultFuture;
    }

    @Override
    public void send(OddsmarketTradingDto.ClientMessage message) {
        currentSessionWebSocket.sendBinary(message.toByteArray());
    }

    @Override
    public boolean isAuthenticatedAndSubscribed() {
        return sessionId != null && currentSessionWebSocket.isOpen();
    }

    @Override
    public boolean isHealthy() {
        return currentTradingFeedState == TradingFeedState.HEALTHY;
    }

    @Override
    public void disconnect() {
        currentSessionWebSocket.sendClose();
        currentSessionWebSocket.disconnect();
    }

    private String constructResumeSessionUrl(long lastConsumedMessageId) {
        return host + RESUME_SESSION_URI + "?sessionId=" + sessionId + "&lastConsumedMessageId=" + lastConsumedMessageId;
    }
}
