package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core;

import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface SessionRecoveryStrategy {
    void recover(TradingFeedConnection connection) throws IOException, WebSocketException, InterruptedException, ExecutionException, TimeoutException;

    int getResumeBufferLimitSeconds();
}
