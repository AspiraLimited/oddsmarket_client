package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeed;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeedReconnectable;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionResult;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TradingFeedDispatcher implements TradingFeedListener {

    private final List<TradingFeed> feeds;
    public static final int NO_ACTIVE_FEED_INDEX = -1;
    @Getter
    private volatile int activeFeedIndex = NO_ACTIVE_FEED_INDEX;
    private final TradingFeedListener tradingFeedListener;
    private Integer resumeBufferLimitSeconds = 60;
    private int resumeRetryInterval = 1000;
    private int newSessionRetryInterval = 3000;

    public TradingFeedDispatcher(List<TradingFeed> feeds, TradingFeedListener tradingFeedListener, TradingFeedNewSessionParams tradingFeedNewSessionParams) {
        for (TradingFeed feed : feeds) {
            feed.setTradingFeedListener(this);
            feed.setTradingFeedDispatcher(this);
        }
        this.feeds = feeds;
        this.tradingFeedListener = tradingFeedListener;
        if (tradingFeedNewSessionParams != null) {
            if (tradingFeedNewSessionParams.getResumeRetryInterval() != null) {
                resumeRetryInterval = tradingFeedNewSessionParams.getResumeRetryInterval();
            }
            if (tradingFeedNewSessionParams.getNewSessionRetryInterval() != null) {
                newSessionRetryInterval = tradingFeedNewSessionParams.getNewSessionRetryInterval();
            }
            if (tradingFeedNewSessionParams.getResumeBufferLimitSeconds() != null) {
                resumeBufferLimitSeconds = tradingFeedNewSessionParams.getResumeBufferLimitSeconds();
            }
        }
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
        for (TradingFeed feed : feeds) {
            feed.send(clientMessage);
        }
    }

    @Override
    public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
        if (tradingFeedListener != null) {
            tradingFeedListener.onConnectError(tradingFeedConnectionStatusCode);
        }
    }

    @SneakyThrows
    @Override
    public void onDisconnected(TradingFeedReconnectable tradingFeedReconnectable) {
        executeSessionRecovery(tradingFeedReconnectable);
    }

    public void disconnectAll() {
        for (TradingFeed feed : feeds) {
            feed.disconnect();
        }
    }

    public void establishNewSessionForAll() throws WebSocketException, IOException, InterruptedException, ExecutionException {
        for (TradingFeed feed : feeds) {
            TradingFeedConnectionResult tradingFeedConnectionResult = feed.establishNewSession().get();
            if (tradingFeedConnectionResult.statusCode != TradingFeedConnectionStatusCode.SUCCESS) {
                throw new ConnectException(tradingFeedConnectionResult.message);
            }
            reevaluateActiveTradingFeed();
        }
    }

    public TradingFeed getActiveTradingFeed() {
        if (activeFeedIndex == NO_ACTIVE_FEED_INDEX) {
            reevaluateActiveTradingFeed();
        }
        TradingFeed activeFeed = feeds.get(activeFeedIndex);
        if (activeFeed.isHealthy()) {
            return activeFeed;
        }
        return null;
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

    public void executeSessionRecovery(TradingFeedReconnectable tradingFeedReconnectable) throws InterruptedException, IOException, WebSocketException, ExecutionException, TimeoutException {
        long disconnectTimestamp = System.currentTimeMillis();
        TradingFeedConnectionStatusCode resumeSessionStatusCode = tradingFeedReconnectable.resumeSession().get(resumeRetryInterval, TimeUnit.MILLISECONDS);
        if (resumeSessionStatusCode.isSuccess()) {
            return;
        }
        reevaluateActiveTradingFeed();
        while (!resumeSessionStatusCode.isSuccess() && !resumeSessionStatusCode.is4xxxErrorCode() && System.currentTimeMillis() - disconnectTimestamp < resumeBufferLimitSeconds * 1000) {
            resumeSessionStatusCode = tradingFeedReconnectable.resumeSession().get(resumeRetryInterval, TimeUnit.MILLISECONDS);
            if (resumeSessionStatusCode == TradingFeedConnectionStatusCode.CONNECTION_FAILED) {
                Thread.sleep(resumeRetryInterval);
            }
        }
        if (resumeSessionStatusCode.isSuccess()) {
            reevaluateActiveTradingFeed();
            return;
        }
        TradingFeedConnectionStatusCode newSessionStatusCode = null;
        while (newSessionStatusCode == null || !newSessionStatusCode.isSuccess()) {
            newSessionStatusCode = tradingFeedReconnectable.establishNewSession().get(newSessionRetryInterval, TimeUnit.MILLISECONDS).statusCode;
        }
        reevaluateActiveTradingFeed();
    }
}
