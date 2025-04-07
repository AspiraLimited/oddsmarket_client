package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionErrorCode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TradingFeedClient {

    private final static String NEW_SESSION_URI = "/trading/new_ws_session";
    private final static String RESUME_SESSION_URI = "/trading/resume_ws_session";

    private final WebSocket newSessionWebSocket;
    private WebSocket resumeSessionWebSocket;
    private String host;
    private volatile String sessionId;

    @Setter
    private TradingFeedListener tradingFeedListener;

    private TradingFeedClient(String host,
                              String apiKey,
                              short tradingFeedId,
                              TradingFeedNewSessionParams tradingFeedNewSessionParams,
                              TradingFeedListener tradingFeedListener) throws IOException {
        this.host = host;
        String websocketUrl = constructNewSessionUrl(host, apiKey, tradingFeedId, tradingFeedNewSessionParams);
        this.newSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
        newSessionWebSocket.addListener(new WebSocketAdapter() {

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] data) throws InvalidProtocolBufferException {
                if (tradingFeedListener != null) {
                    OddsmarketTradingDto.ServerMessage serverMessage = OddsmarketTradingDto.ServerMessage.parseFrom(data);

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
                if (tradingFeedListener != null) {
                    TradingFeedConnectionErrorCode tradingFeedConnectionErrorCode = TradingFeedConnectionErrorCode.detectTradingFeedConnectionErrorCodeByErrorCode(statusCode);
                    tradingFeedListener.onConnectError(tradingFeedConnectionErrorCode);
                }
            }
        });

        this.tradingFeedListener = tradingFeedListener;
    }

    private static String constructNewSessionUrl(String host, String apiKey,
                                                 short tradingFeedId, TradingFeedNewSessionParams tradingFeedNewSessionParams) {
        return host + NEW_SESSION_URI + "?apiKey=" + apiKey + "&tradingFeedId=" + tradingFeedId + (tradingFeedNewSessionParams != null ? tradingFeedNewSessionParams.toQueryString() : "");
    }

    public static TradingFeedClient authenticateAndSubscribe(String host,
                                                             String apiKey,
                                                             short tradingFeedId,
                                                             TradingFeedListener tradingFeedListener) throws IOException, WebSocketException {

        return TradingFeedClient.authenticateAndSubscribe(host, apiKey, tradingFeedId, tradingFeedListener, null);
    }


    public static TradingFeedClient authenticateAndSubscribe(String host,
                                                             String apiKey,
                                                             short tradingFeedId,
                                                             TradingFeedListener tradingFeedListener,
                                                             TradingFeedNewSessionParams tradingFeedNewSessionParams) throws IOException, WebSocketException {
        TradingFeedClient client = new TradingFeedClient(
                host,
                apiKey,
                tradingFeedId,
                tradingFeedNewSessionParams,
                tradingFeedListener
        );
        client.authenticateAndSubscribe();
        return client;
    }

    public void resumeSession(long lastConsumedMessageId) throws IOException, WebSocketException {
        if (resumeSessionWebSocket == null) {
            String websocketUrl = constructResumeSessionUrl(lastConsumedMessageId);
            this.resumeSessionWebSocket = new WebSocketFactory().createSocket(websocketUrl);
            resumeSessionWebSocket.addListener(new WebSocketAdapter() {

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
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
                    if (tradingFeedListener != null) {
                        TradingFeedConnectionErrorCode tradingFeedConnectionErrorCode = TradingFeedConnectionErrorCode.detectTradingFeedConnectionErrorCodeByErrorCode(statusCode);
                        tradingFeedListener.onConnectError(tradingFeedConnectionErrorCode);
                    }
                }
            });
            resumeSessionWebSocket.connect();
        }
    }

    private String constructResumeSessionUrl(long lastConsumedMessageId) {
        return host + RESUME_SESSION_URI + "?sessionId=" + sessionId + "&lastConsumedMessageId=" + lastConsumedMessageId;
    }

    public TradingFeedClient authenticateAndSubscribe() throws WebSocketException {
        newSessionWebSocket.connect();
        return this;
    }

    public boolean isAuthenticatedAndSubscribed() {
        return sessionId != null && newSessionWebSocket.isOpen();
    }

    public TradingFeedClient disconnect() {
        newSessionWebSocket.sendClose();
        newSessionWebSocket.disconnect();
        if (resumeSessionWebSocket != null) {
            resumeSessionWebSocket.sendClose();
            resumeSessionWebSocket.disconnect();
        }
        return this;
    }

    public void send(OddsmarketTradingDto.ClientMessage message) {
        newSessionWebSocket.sendBinary(message.toByteArray());
    }
}
