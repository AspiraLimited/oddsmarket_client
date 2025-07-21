package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageProcessor {

    private final TradingFeedListener listener;
    private final ConnectionHealthManager connectionHealthManager;

    public void processMessage(int connectionPriority, OddsmarketTradingDto.ServerMessage message) {
        if (connectionPriority == connectionHealthManager.getActiveConnectionId()) {
            listener.onServerMessage(message);
        }
    }

    public void processError(int connectionPriority, TradingFeedConnectionStatusCode statusCode) {
        if (connectionPriority == connectionHealthManager.getActiveConnectionId()) {
            listener.onConnectError(statusCode);
        }
    }

}
