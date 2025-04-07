package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum TradingFeedConnectionErrorCode {
    BAD_REQUEST(4000),
    AUTHENTICATION_FAILED(4001),
    SUBSCRIPTION_FAILED(4003),
    SESSION_NOT_FOUND(4004),
    UNDEFINED(-1);

    public final int errorCode;
    public static final Map<Integer, TradingFeedConnectionErrorCode> tradingFeedConnectionErrorCodeByErrorCode = new HashMap<>();

    static {
        for (TradingFeedConnectionErrorCode value : TradingFeedConnectionErrorCode.values()) {
            tradingFeedConnectionErrorCodeByErrorCode.put(value.errorCode, value);
        }
    }

    public static TradingFeedConnectionErrorCode detectTradingFeedConnectionErrorCodeByErrorCode(int errorCode) {
        return tradingFeedConnectionErrorCodeByErrorCode.getOrDefault(errorCode, UNDEFINED);
    }


}
