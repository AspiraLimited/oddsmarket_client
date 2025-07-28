package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.ConnectionSelectionStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
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
            SessionRecoveryStrategy sessionRecoveryStrategy,
            ConnectionSelectionStrategy connectionSelectionStrategy
    ) {
        super(
                List.of(primaryHost, fallbackHost),
                tradingFeedSubscriptionConfig,
                tradingFeedListener,
                sessionRecoveryStrategy,
                connectionSelectionStrategy
        );
    }

}
