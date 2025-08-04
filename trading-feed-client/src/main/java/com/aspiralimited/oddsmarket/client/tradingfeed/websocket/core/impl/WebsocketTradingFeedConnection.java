package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl;


import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedSubscriptionConfig;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionResult;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class WebsocketTradingFeedConnection extends WebSocketAdapter implements TradingFeedConnection {
    private final static String NEW_SESSION_URI = "/trading/new_ws_session";
    private final static String RESUME_SESSION_URI = "/trading/resume_ws_session";

    private volatile String sessionId;
    @Getter
    private final int connectionId;
    private final String host;
    private WebSocket currentSessionWebSocket;
    private final TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig;
    private final MessageProcessor messageProcessor;
    private final ConnectionHealthManager connectionHealthManager;
    private final SessionRecoveryStrategy sessionRecoveryStrategy;
    private CompletableFuture<TradingFeedConnectionResult> resultFuture;
    @Setter
    private volatile long lastConsumedMessageId;
    @Setter
    @Getter
    private volatile boolean active;
    private final boolean json;
    private volatile boolean forceDisconnect;


    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public CompletableFuture<TradingFeedConnectionResult> establishNewSession() throws IOException, InterruptedException, WebSocketException {
        String websocketUrl = constructNewSessionUrl(
                host,
                tradingFeedSubscriptionConfig
        );
        resultFuture = new CompletableFuture<>();
        currentSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
        currentSessionWebSocket.addListener(this);
        try {
            currentSessionWebSocket.connect();
        } catch (Exception e) {
            resultFuture.complete(TradingFeedConnectionResult.error(TradingFeedConnectionStatusCode.CONNECTION_FAILED, e.getMessage()));
        }
        return resultFuture;
    }


    private String constructNewSessionUrl(String host, TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig) {
        String result = host + NEW_SESSION_URI + "?" + tradingFeedSubscriptionConfig.toQueryString();
        if (json) {
            result = result + "&json=true";
        }
        if (sessionRecoveryStrategy != null) {
            result = result + "&resumeBufferLimitSeconds=" + sessionRecoveryStrategy.getResumeBufferLimitSeconds();
        }
        return result;
    }

    private void sendAck(long messageId) {
        OddsmarketTradingDto.Ack ack = OddsmarketTradingDto.Ack.newBuilder()
                .setMessageId(messageId)
                .build();
        OddsmarketTradingDto.ClientMessage clientMessage = OddsmarketTradingDto.ClientMessage.newBuilder()
                .setAck(ack)
                .build();
        send(clientMessage);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        if (connectionHealthManager != null) {
            connectionHealthManager.updateConnectionState(connectionId, TradingFeedState.HEALTHY);
        }
        resultFuture.complete(TradingFeedConnectionResult.success());
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws InterruptedException, IOException, WebSocketException, ExecutionException, TimeoutException {
        if (connectionHealthManager != null) {
            connectionHealthManager.updateConnectionState(connectionId, TradingFeedState.UNHEALTHY);
        }
        if (sessionRecoveryStrategy != null && !forceDisconnect) {
            sessionRecoveryStrategy.recover(WebsocketTradingFeedConnection.this);
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
        messageProcessor.processMessage(connectionId, serverMessage);
        sendAck(serverMessage.getMessageId());
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) {
        int statusCode = frame.getCloseCode();
        TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode = TradingFeedConnectionStatusCode.detectTradingFeedConnectionErrorCodeByErrorCode(statusCode);
        messageProcessor.processError(connectionId, tradingFeedConnectionStatusCode);
        if (connectionHealthManager != null) {
            connectionHealthManager.updateConnectionState(connectionId, TradingFeedState.UNHEALTHY);
        }
        resultFuture.complete(TradingFeedConnectionResult.error(tradingFeedConnectionStatusCode, frame.getCloseReason()));

    }


    @Override
    public CompletableFuture<TradingFeedConnectionResult> resumeSession() throws IOException {
        if (sessionId == null) {
            return CompletableFuture.completedFuture(TradingFeedConnectionResult.error(TradingFeedConnectionStatusCode.BAD_REQUEST, "sessionId is null"));
        }
        String websocketUrl = constructResumeSessionUrl(lastConsumedMessageId);
        currentSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
        resultFuture = new CompletableFuture<>();
        currentSessionWebSocket.addListener(this);
        try {
            currentSessionWebSocket.connect();
        } catch (Exception e) {
            resultFuture.complete(TradingFeedConnectionResult.error(TradingFeedConnectionStatusCode.CONNECTION_FAILED, e.getMessage()));
        }
        return resultFuture;
    }

    @Override
    public void send(OddsmarketTradingDto.ClientMessage message) {
        currentSessionWebSocket.sendBinary(message.toByteArray());
    }

    @Override
    public boolean isConnected() {
        return sessionId != null && currentSessionWebSocket.isOpen();
    }

    @Override
    public void disconnect(boolean force) {
        this.forceDisconnect = force;
        currentSessionWebSocket.sendClose();
        currentSessionWebSocket.disconnect();
    }

    private String constructResumeSessionUrl(long lastConsumedMessageId) {
        return host + RESUME_SESSION_URI + "?sessionId=" + sessionId + "&lastConsumedMessageId=" + lastConsumedMessageId;
    }

}
