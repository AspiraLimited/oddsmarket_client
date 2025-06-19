package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TradingFeedConnectionResult {
    public final TradingFeedConnectionStatusCode statusCode;
    public final String message;

    public static TradingFeedConnectionResult success() {
        return new TradingFeedConnectionResult(TradingFeedConnectionStatusCode.SUCCESS, null);
    }

    public static TradingFeedConnectionResult error(TradingFeedConnectionStatusCode statusCode, String errorMessage) {
        return new TradingFeedConnectionResult(statusCode, errorMessage);
    }
}
