package com.aspiralimited.oddsmarket.client.wsevents;

import com.aspiralimited.oddsmarket.client.wsevents.message.WebSocketMessage;

public interface OddsmarketWebSocketListener {

    default void onConnected() {
    }

    default void onDisconnected(int code, String reason, boolean closedByServer) {
    }

    default void onReconnecting() {
    }

    default void onReconnected() {
    }

    default void onWebSocketMessage(WebSocketMessage message) {
    }

    default void onTextMessage(String message) {
    }

    default void onError(Throwable cause) {
    }

    default void handleUncaughtException(Throwable cause) {
    }
}
