package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

public class TradingFeedMulticonnectionClient extends AbstractTradingFeedClient {

    @Builder
    public TradingFeedMulticonnectionClient(
            @NonNull String primaryHost,
            @NonNull String fallbackHost,
            @NonNull TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig,
            @NonNull TradingFeedListener tradingFeedListener,
            boolean autoReconnect,
            int maxReconnectAttempts,
            Integer resumeBufferLimitSeconds
    ) {
        super(List.of(primaryHost, fallbackHost), tradingFeedSubscriptionConfig, tradingFeedListener,
                autoReconnect, maxReconnectAttempts, resumeBufferLimitSeconds);
    }

}
