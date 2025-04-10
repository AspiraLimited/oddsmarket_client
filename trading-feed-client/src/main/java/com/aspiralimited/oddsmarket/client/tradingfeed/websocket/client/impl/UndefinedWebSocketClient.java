package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.impl;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.WebSocketClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.WebsocketConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

public class UndefinedWebSocketClient implements WebSocketClient {
    @Override
    public WebsocketConnectionStatusCode establishNewSession() throws IOException, InterruptedException, WebSocketException {
        return null;
    }

    @Override
    public WebsocketConnectionStatusCode resumeSession() throws IOException, WebSocketException, InterruptedException {
        return null;
    }

    @Override
    public void send(OddsmarketTradingDto.ClientMessage message) {

    }

    @Override
    public boolean isAuthenticatedAndSubscribed() {
        return false;
    }

    @Override
    public void disconnect() {

    }
}
