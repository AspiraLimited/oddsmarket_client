package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedDispatcher;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface TradingFeed {

    void setTradingFeedListener(TradingFeedListener tradingFeedListener);

    void setTradingFeedDispatcher(TradingFeedDispatcher tradingFeedDispatcher);

    String getSessionId();

    CompletableFuture<TradingFeedConnectionStatusCode> establishNewSession() throws IOException, InterruptedException, WebSocketException;

    CompletableFuture<TradingFeedConnectionStatusCode> resumeSession() throws IOException, WebSocketException, InterruptedException;

    void send(OddsmarketTradingDto.ClientMessage message);

    boolean isAuthenticatedAndSubscribed();

    boolean isHealthy();

    void disconnect();
}