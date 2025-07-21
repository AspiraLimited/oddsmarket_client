package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionHealthManager {

    private final AtomicInteger activeConnectionId = new AtomicInteger(0);
    private final Map<Integer, TradingFeedState> connectionStates = new ConcurrentHashMap<>();

    public void updateConnectionState(int connectionId, TradingFeedState state) {
        connectionStates.put(connectionId, state);
        recalculateActiveConnection();
    }

    public int getActiveConnectionId() {
        return activeConnectionId.get();
    }

    private synchronized void recalculateActiveConnection() {
        connectionStates.entrySet().stream()
                .filter(entry -> entry.getValue() == TradingFeedState.HEALTHY)
                .min(Map.Entry.comparingByKey())
                .ifPresentOrElse(
                        entry -> activeConnectionId.set(entry.getKey()),
                        () -> {
                        }
                );
    }
}
