package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionResult;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface TradingFeedConnection {
    int getConnectionId();

    String getSessionId();

    CompletableFuture<TradingFeedConnectionResult> establishNewSession() throws IOException, InterruptedException, WebSocketException;

    CompletableFuture<TradingFeedConnectionResult> resumeSession() throws IOException;

    void send(OddsmarketTradingDto.ClientMessage message);

    boolean isConnected();

    void disconnect(boolean force);
}