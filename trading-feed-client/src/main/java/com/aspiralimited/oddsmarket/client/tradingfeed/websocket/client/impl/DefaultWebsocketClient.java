package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.impl;


import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedNewSessionParams;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.WebSocketClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.WebsocketConnectionStatusCode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class DefaultWebsocketClient implements WebSocketClient {
    private final static String NEW_SESSION_URI = "/trading/new_ws_session";
    private final static String RESUME_SESSION_URI = "/trading/resume_ws_session";

    private volatile String sessionId;
    private final String host;
    private WebSocket currentSessionWebSocket;
    private final String apiKey;
    private final short tradingFeedId;
    private final TradingFeedNewSessionParams tradingFeedNewSessionParams;
    private final TradingFeedListener tradingFeedListener;
    private volatile long lastConsumedMessageId;
    @Setter
    private WebSocketDisconnectListener webSocketDisconnectListener;

    @Override
    public WebsocketConnectionStatusCode establishNewSession() throws IOException, InterruptedException, WebSocketException {
        String websocketUrl = constructNewSessionUrl(host, apiKey, tradingFeedId, tradingFeedNewSessionParams);
        CountDownLatch connectionCompletionLatch = new CountDownLatch(1);
        AtomicReference<WebsocketConnectionStatusCode> result = new AtomicReference<>();
        currentSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
        currentSessionWebSocket.addListener(new WebSocketAdapter() {
            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                result.set(WebsocketConnectionStatusCode.SUCCESS);
                connectionCompletionLatch.countDown();
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws InterruptedException, IOException, WebSocketException {
                if (webSocketDisconnectListener != null && sessionId != null) {
                    webSocketDisconnectListener.onDisconnected();
                }
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] data) throws InvalidProtocolBufferException {
                OddsmarketTradingDto.ServerMessage serverMessage = OddsmarketTradingDto.ServerMessage.parseFrom(data);
                lastConsumedMessageId = serverMessage.getMessageId();
                if (tradingFeedListener != null) {
                    if (serverMessage.getPayloadCase() == OddsmarketTradingDto.ServerMessage.PayloadCase.SESSIONSTART) {
                        OddsmarketTradingDto.SessionStart sessionStart = serverMessage.getSessionStart();
                        sessionId = sessionStart.getSessionId();
                    }
                    tradingFeedListener.onServerMessage(serverMessage);
                }
            }

            @Override
            public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) {
                int statusCode = frame.getCloseCode();
                WebsocketConnectionStatusCode websocketConnectionStatusCode = WebsocketConnectionStatusCode.detectTradingFeedConnectionErrorCodeByErrorCode(statusCode);
                result.set(websocketConnectionStatusCode);
                connectionCompletionLatch.countDown();
                if (tradingFeedListener != null) {
                    tradingFeedListener.onConnectError(websocketConnectionStatusCode);
                }
            }
        });
        currentSessionWebSocket.connect();
        connectionCompletionLatch.await();
        return result.get();
    }

    private static String constructNewSessionUrl(String host, String apiKey,
                                                 short tradingFeedId, TradingFeedNewSessionParams tradingFeedNewSessionParams) {
        return host + NEW_SESSION_URI + "?apiKey=" + apiKey + "&tradingFeedId=" + tradingFeedId + (tradingFeedNewSessionParams != null ? tradingFeedNewSessionParams.toQueryString() : "");
    }

    @Override
    public WebsocketConnectionStatusCode resumeSession() throws IOException, WebSocketException, InterruptedException {
        if (sessionId == null) {
            return WebsocketConnectionStatusCode.BAD_REQUEST;
        }
        String websocketUrl = constructResumeSessionUrl(lastConsumedMessageId);
        currentSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
        CountDownLatch connectionCompletionLatch = new CountDownLatch(1);
        AtomicReference<WebsocketConnectionStatusCode> result = new AtomicReference<>();
        currentSessionWebSocket.addListener(new WebSocketAdapter() {

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                result.set(WebsocketConnectionStatusCode.SUCCESS);
                connectionCompletionLatch.countDown();
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws IOException, InterruptedException, WebSocketException {
                if (webSocketDisconnectListener != null) {
                    webSocketDisconnectListener.onDisconnected();
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
                int statusCode = frame.getCloseCode();
                WebsocketConnectionStatusCode websocketConnectionStatusCode = WebsocketConnectionStatusCode.detectTradingFeedConnectionErrorCodeByErrorCode(statusCode);
                result.set(websocketConnectionStatusCode);
                connectionCompletionLatch.countDown();
                if (tradingFeedListener != null) {
                    tradingFeedListener.onConnectError(websocketConnectionStatusCode);
                }
            }
        });
        currentSessionWebSocket.connect();
        connectionCompletionLatch.await();
        return result.get();
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
    public void disconnect() {
        currentSessionWebSocket.sendClose();
        currentSessionWebSocket.disconnect();
    }

    private String constructResumeSessionUrl(long lastConsumedMessageId) {
        return host + RESUME_SESSION_URI + "?sessionId=" + sessionId + "&lastConsumedMessageId=" + lastConsumedMessageId;
    }
}
