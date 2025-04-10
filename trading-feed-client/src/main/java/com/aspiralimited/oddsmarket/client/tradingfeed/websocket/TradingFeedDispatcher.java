package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.CurrentTradingFeedState;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.WebsocketConnectionStatusCode;

public class TradingFeedDispatcher implements TradingFeedListener {

    private CurrentTradingFeedState currentTradingFeedState;
    private TradingFeedListener tradingFeedListener;

    @Override
    public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {

    }

    @Override
    public void onConnectError(WebsocketConnectionStatusCode websocketConnectionStatusCode) {

    }
}
