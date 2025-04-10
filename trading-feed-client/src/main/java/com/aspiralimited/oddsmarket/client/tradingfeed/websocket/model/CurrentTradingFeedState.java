package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;


public class CurrentTradingFeedState {
    private final AtomicReference<TradingFeedState> currentTradingFeedState = new AtomicReference<>();

    public CurrentTradingFeedState(TradingFeedState tradingFeedState) {
        currentTradingFeedState.set(tradingFeedState);
    }

    public TradingFeedState get() {
        return currentTradingFeedState.get();
    }

    public boolean compareAndSet(TradingFeedState expectedValue, TradingFeedState newValue) {
        return currentTradingFeedState.compareAndSet(expectedValue, newValue);
    }

    public boolean isPrimaryTradingFeed() {
        return currentTradingFeedState.get().isPrimaryState();
    }

    public boolean isFallbackTradingFeed() {
        return currentTradingFeedState.get().isFallbackState();
    }
}
