package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.WebSocketClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.impl.DefaultWebsocketClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.impl.UndefinedWebSocketClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.CurrentTradingFeedState;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.WebsocketConnectionStatusCode;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.recovery.WebSocketSessionRecoveryStrategy;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

public class TradingFeedClient {

    private final DefaultWebsocketClient primaryWebSocketClient;
    private final DefaultWebsocketClient fallbackWebSocketClient;
    private final UndefinedWebSocketClient undefinedWebSocketClient = new UndefinedWebSocketClient();

    private final CurrentTradingFeedState currentTradingFeedState;

    private TradingFeedClient(String host,
                              String fallbackHost,
                              String apiKey,
                              short tradingFeedId,
                              TradingFeedNewSessionParams tradingFeedNewSessionParams,
                              TradingFeedListener tradingFeedListener) throws IOException {


        primaryWebSocketClient = new DefaultWebsocketClient(
                host,
                apiKey,
                tradingFeedId,
                tradingFeedNewSessionParams,
                new TradingFeedListener() {
                    @Override
                    public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
                        if (currentTradingFeedState.isPrimaryTradingFeed()) {
                            tradingFeedListener.onServerMessage(serverMessage);
                        }
                    }

                    @Override
                    public void onConnectError(WebsocketConnectionStatusCode websocketConnectionStatusCode) {
                        tradingFeedListener.onConnectError(websocketConnectionStatusCode);
                    }
                }
        );
        fallbackWebSocketClient = new DefaultWebsocketClient(
                fallbackHost,
                apiKey,
                tradingFeedId,
                tradingFeedNewSessionParams,
                new TradingFeedListener() {
                    @Override
                    public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
                        if (currentTradingFeedState.isFallbackTradingFeed()) {
                            tradingFeedListener.onServerMessage(serverMessage);
                        }
                    }

                    @Override
                    public void onConnectError(WebsocketConnectionStatusCode websocketConnectionStatusCode) {
                        tradingFeedListener.onConnectError(websocketConnectionStatusCode);
                    }
                }

        );
        currentTradingFeedState = new CurrentTradingFeedState(TradingFeedState.HEALTHY);
        WebSocketSessionRecoveryStrategy webSocketSessionRecoveryStrategy = new WebSocketSessionRecoveryStrategy(
                currentTradingFeedState,
                primaryWebSocketClient,
                fallbackWebSocketClient
        );
        primaryWebSocketClient.setWebSocketDisconnectListener(webSocketSessionRecoveryStrategy::executePrimarySessionRecovery);
        fallbackWebSocketClient.setWebSocketDisconnectListener(webSocketSessionRecoveryStrategy::executeFallbackSessionRecovery);
    }


    public static TradingFeedClient authenticateAndSubscribe(String host,
                                                             String fallbackHost,
                                                             String apiKey,
                                                             short tradingFeedId,
                                                             TradingFeedListener tradingFeedListener) throws IOException, WebSocketException, InterruptedException {

        return TradingFeedClient.authenticateAndSubscribe(host, fallbackHost, apiKey, tradingFeedId, tradingFeedListener, null);
    }


    public static TradingFeedClient authenticateAndSubscribe(String host,
                                                             String fallbackHost,
                                                             String apiKey,
                                                             short tradingFeedId,
                                                             TradingFeedListener tradingFeedListener,
                                                             TradingFeedNewSessionParams tradingFeedNewSessionParams) throws IOException, WebSocketException, InterruptedException {
        TradingFeedClient client = new TradingFeedClient(
                host,
                fallbackHost,
                apiKey,
                tradingFeedId,
                tradingFeedNewSessionParams,
                tradingFeedListener
        );
        client.authenticateAndSubscribe();
        return client;
    }


    public TradingFeedClient authenticateAndSubscribe() throws WebSocketException, IOException, InterruptedException {
        primaryWebSocketClient.establishNewSession();
        fallbackWebSocketClient.establishNewSession();
        return this;
    }

    public boolean isAuthenticatedAndSubscribed() {
        return getCurrentWebSocketClient().isAuthenticatedAndSubscribed();
    }

    public TradingFeedClient disconnect() {
        primaryWebSocketClient.disconnect();
        fallbackWebSocketClient.disconnect();
        return this;
    }

    public void send(OddsmarketTradingDto.ClientMessage message) {
        getCurrentWebSocketClient().send(message);
    }

    private WebSocketClient getCurrentWebSocketClient() {
        if (currentTradingFeedState.isPrimaryTradingFeed()) {
            return primaryWebSocketClient;
        } else if (currentTradingFeedState.isFallbackTradingFeed()) {
            return fallbackWebSocketClient;
        } else {
            return undefinedWebSocketClient;
        }
    }
}
