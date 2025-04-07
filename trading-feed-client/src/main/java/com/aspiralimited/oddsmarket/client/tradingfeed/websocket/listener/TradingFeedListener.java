package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionErrorCode;

public interface TradingFeedListener {
    void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage);
    void onConnectError(TradingFeedConnectionErrorCode tradingFeedConnectionErrorCode);
}
