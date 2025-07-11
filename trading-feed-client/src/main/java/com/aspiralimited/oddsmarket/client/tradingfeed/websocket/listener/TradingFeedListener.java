package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeedReconnectable;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;

public interface TradingFeedListener {
    void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage);

    void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode);

    void onDisconnected(TradingFeedReconnectable tradingFeedReconnectable);
}
