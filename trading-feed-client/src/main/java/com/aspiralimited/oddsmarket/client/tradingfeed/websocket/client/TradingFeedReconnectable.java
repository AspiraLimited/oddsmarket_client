package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionResult;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface TradingFeedReconnectable {
    CompletableFuture<TradingFeedConnectionResult> establishNewSession() throws IOException, InterruptedException, WebSocketException;

    CompletableFuture<TradingFeedConnectionStatusCode> resumeSession() throws IOException;
}
