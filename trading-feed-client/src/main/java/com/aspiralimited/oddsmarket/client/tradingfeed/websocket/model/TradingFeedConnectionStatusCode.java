package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum TradingFeedConnectionStatusCode {
    SUCCESS(null),
    CONNECTION_FAILED(null),
    TIMEOUT(null),
    BAD_REQUEST(4000),
    AUTHENTICATION_FAILED(4001),
    SUBSCRIPTION_FAILED(4003),
    SESSION_NOT_FOUND(4004),
    UNDEFINED(null),
    JSON_ACCESS_TIMEOUT(4005);

    public final Integer errorCode;
    public static final Map<Integer, TradingFeedConnectionStatusCode> tradingFeedConnectionErrorCodeByErrorCode = new HashMap<>();

    static {
        for (TradingFeedConnectionStatusCode value : TradingFeedConnectionStatusCode.values()) {
            if (value.errorCode != null) {
                tradingFeedConnectionErrorCodeByErrorCode.put(value.errorCode, value);
            }
        }
    }

    public static TradingFeedConnectionStatusCode detectTradingFeedConnectionErrorCodeByErrorCode(int errorCode) {
        return tradingFeedConnectionErrorCodeByErrorCode.getOrDefault(errorCode, UNDEFINED);
    }

    public boolean is4xxxErrorCode() {
        return errorCode != null;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

}
