package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeed;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.impl.WebsocketTradingFeed;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TradingFeedClient {

    private final TradingFeedDispatcher tradingFeedDispatcher;

    private TradingFeedClient(String host,
                              String fallbackHost,
                              String apiKey,
                              short tradingFeedId,
                              TradingFeedNewSessionParams tradingFeedNewSessionParams,
                              TradingFeedListener tradingFeedListener) throws IOException {


        TradingFeed primaryTradingFeed = new WebsocketTradingFeed(
                host,
                apiKey,
                tradingFeedId,
                tradingFeedNewSessionParams
        );
        TradingFeed fallbackTradingFeed = new WebsocketTradingFeed(
                fallbackHost,
                apiKey,
                tradingFeedId,
                tradingFeedNewSessionParams
        );
        tradingFeedDispatcher = new TradingFeedDispatcher(
                List.of(primaryTradingFeed, fallbackTradingFeed),
                tradingFeedListener
        );
    }


    public static TradingFeedClient authenticateAndSubscribe(String host,
                                                             String fallbackHost,
                                                             String apiKey,
                                                             short tradingFeedId,
                                                             TradingFeedListener tradingFeedListener) throws IOException, WebSocketException, InterruptedException, ExecutionException {

        return TradingFeedClient.authenticateAndSubscribe(host, fallbackHost, apiKey, tradingFeedId, tradingFeedListener, null);
    }


    public static TradingFeedClient authenticateAndSubscribe(String host,
                                                             String fallbackHost,
                                                             String apiKey,
                                                             short tradingFeedId,
                                                             TradingFeedListener tradingFeedListener,
                                                             TradingFeedNewSessionParams tradingFeedNewSessionParams) throws IOException, WebSocketException, InterruptedException, ExecutionException {
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


    public TradingFeedClient authenticateAndSubscribe() throws WebSocketException, IOException, InterruptedException, ExecutionException {
        tradingFeedDispatcher.establishNewSessionForAll();
        return this;
    }

    public boolean isAuthenticatedAndSubscribed() {
        TradingFeed tradingFeed = tradingFeedDispatcher.getActiveTradingFeed();
        if (tradingFeed != null) {
            return tradingFeed.isAuthenticatedAndSubscribed();
        } else {
            return false;
        }
    }

    public TradingFeedClient disconnect() {
        tradingFeedDispatcher.disconnectAll();
        return this;
    }

    public void send(OddsmarketTradingDto.ClientMessage message) {
        tradingFeedDispatcher.getActiveTradingFeed().send(message);
    }

    public String getCurrentSessionId() {
        TradingFeed tradingFeed = tradingFeedDispatcher.getActiveTradingFeed();
        return tradingFeed != null ? tradingFeed.getSessionId() : null;
    }

    public int getActiveFeedIndex() {
        return tradingFeedDispatcher.getActiveFeedIndex();
    }


}
