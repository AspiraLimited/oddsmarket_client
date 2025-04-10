package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.WebsocketConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

public interface WebSocketClient {
    WebsocketConnectionStatusCode establishNewSession() throws IOException, InterruptedException, WebSocketException;

    WebsocketConnectionStatusCode resumeSession() throws IOException, WebSocketException, InterruptedException;

    void send(OddsmarketTradingDto.ClientMessage message);

    boolean isAuthenticatedAndSubscribed();

    void disconnect();
}