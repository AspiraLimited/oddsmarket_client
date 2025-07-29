package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.ConnectionHealthManager;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.MessageProcessor;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.WebsocketTradingFeedConnection;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class TradingFeedConnectionFactory {

    private final TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig;
    private final MessageProcessor messageProcessor;
    private final ConnectionHealthManager connectionHealthManager;
    private final SessionRecoveryStrategy sessionRecoveryStrategy;
    private final AtomicInteger connectionIdCounter = new AtomicInteger();

    public TradingFeedConnection constructTradingFeedConnection(String host) {
        return new WebsocketTradingFeedConnection(
                connectionIdCounter.incrementAndGet(),
                host,
                tradingFeedSubscriptionConfig,
                messageProcessor,
                connectionHealthManager,
                sessionRecoveryStrategy
        );
    }
}
