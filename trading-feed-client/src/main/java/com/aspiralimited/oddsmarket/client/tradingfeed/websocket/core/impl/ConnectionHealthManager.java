package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.ConnectionSelectionStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionHealthManager {

    private final AtomicInteger activeConnectionId = new AtomicInteger(0);
    private final Map<Integer, TradingFeedState> connectionStates = new ConcurrentHashMap<>();
    private final ConnectionSelectionStrategy selectionStrategy;

    public ConnectionHealthManager(ConnectionSelectionStrategy selectionStrategy) {
        this.selectionStrategy = selectionStrategy;
    }

    public void updateConnectionState(int connectionId, TradingFeedState state) {
        connectionStates.put(connectionId, state);
        recalculateActiveConnection();
    }

    public int getActiveConnectionId() {
        return activeConnectionId.get();
    }

    private synchronized void recalculateActiveConnection() {
        int selected = selectionStrategy.selectActiveConnection(connectionStates);
        if (selected != -1) {
            activeConnectionId.set(selected);
        }
    }
}
