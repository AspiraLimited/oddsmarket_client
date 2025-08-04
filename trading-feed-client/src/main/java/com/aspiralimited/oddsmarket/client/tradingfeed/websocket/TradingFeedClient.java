package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.ConnectionSelectionStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.DefaultSessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

public class TradingFeedClient extends AbstractTradingFeedClient {

    @Builder
    public TradingFeedClient(
            @NonNull String host,
            @NonNull TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig,
            @NonNull TradingFeedListener tradingFeedListener,
            SessionRecoveryStrategy sessionRecoveryStrategy,
            ConnectionSelectionStrategy connectionSelectionStrategy,
            boolean json
    ) {
        super(List.of(host), tradingFeedSubscriptionConfig, tradingFeedListener, sessionRecoveryStrategy, connectionSelectionStrategy, json);
    }

}
