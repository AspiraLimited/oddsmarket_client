package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;

import java.util.Map;

public interface ConnectionSelectionStrategy {
    int selectActiveConnection(Map<Integer, TradingFeedState> connectionStates);
}
