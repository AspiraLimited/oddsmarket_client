package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeed;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TradingFeedDispatcher implements TradingFeedListener {

    private final List<TradingFeed> feeds;
    private static final int NO_ACTIVE_FEED_INDEX = -1;
    private volatile int activeFeedIndex = NO_ACTIVE_FEED_INDEX;
    private final TradingFeedListener tradingFeedListener;

    public TradingFeedDispatcher(List<TradingFeed> feeds, TradingFeedListener tradingFeedListener) {
        for (TradingFeed feed : feeds) {
            feed.setTradingFeedListener(this);
            feed.setTradingFeedDispatcher(this);
        }
        this.feeds = feeds;
        this.tradingFeedListener = tradingFeedListener;
    }

    @Override
    public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
        if (tradingFeedListener != null) {
            tradingFeedListener.onServerMessage(serverMessage);
            sendAck(serverMessage.getMessageId());
        }
    }

    private void sendAck(long messageId) {
        OddsmarketTradingDto.Ack ack = OddsmarketTradingDto.Ack.newBuilder()
                .setMessageId(messageId)
                .build();
        OddsmarketTradingDto.ClientMessage clientMessage = OddsmarketTradingDto.ClientMessage.newBuilder()
                .setAck(ack)
                .build();
        getActiveTradingFeed().send(clientMessage);
    }

    @Override
    public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
        if (tradingFeedListener != null) {
            tradingFeedListener.onConnectError(tradingFeedConnectionStatusCode);
        }
    }

    public void disconnectAll() {
        for (TradingFeed feed : feeds) {
            feed.disconnect();
        }
    }

    public void establishNewSessionForAll() throws WebSocketException, IOException, InterruptedException, ExecutionException {
        for (TradingFeed feed : feeds) {
            feed.establishNewSession().get();
            reevaluateActiveTradingFeed();
        }
    }

    public TradingFeed getActiveTradingFeed() {
        TradingFeed activeFeed = feeds.get(activeFeedIndex);
        if (activeFeed.isHealthy()) {
            return activeFeed;
        }
        return reevaluateActiveTradingFeed();
    }

    public TradingFeed reevaluateActiveTradingFeed() {
        for (int i = 0; i < feeds.size(); i++) {
            if (feeds.get(i).isHealthy()) {
                activeFeedIndex = i;
                return feeds.get(i);
            }
        }
        activeFeedIndex = NO_ACTIVE_FEED_INDEX;
        return null;
    }

    public boolean isTradingFeedActive(TradingFeed tradingFeed) {
        if (activeFeedIndex == NO_ACTIVE_FEED_INDEX) {
            return false;
        }
        return feeds.get(activeFeedIndex) == tradingFeed;
    }
}
