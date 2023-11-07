package com.aspiralimited.oddsmarket.client.v4.websocket.handlers;

import com.neovisionaries.ws.client.WebSocket;

public interface PongTimeoutListener {

    void onPongTimeout(WebSocket webSocket);
}
