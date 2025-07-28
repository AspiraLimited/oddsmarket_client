package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.ConnectionSelectionStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;

import java.util.Map;

public class LowestIdHealthyConnectionStrategy implements ConnectionSelectionStrategy {
    @Override
    public int selectActiveConnection(Map<Integer, TradingFeedState> connectionStates) {
        return connectionStates.entrySet().stream()
                .filter(e -> e.getValue() == TradingFeedState.HEALTHY)
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }
}

