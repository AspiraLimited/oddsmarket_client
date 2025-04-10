package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.impl;

import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

public interface WebSocketDisconnectListener {
    void onDisconnected() throws WebSocketException, IOException, InterruptedException;
}
