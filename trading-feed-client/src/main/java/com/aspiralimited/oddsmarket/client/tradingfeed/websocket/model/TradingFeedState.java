package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model;

public enum TradingFeedState {
    HEALTHY, PRIMARY, FALLBACK, UNHEALTHY;

    public boolean isPrimaryState() {
        return this == HEALTHY || this == PRIMARY;
    }

    public boolean isFallbackState() {
        return this == FALLBACK;
    }
}
