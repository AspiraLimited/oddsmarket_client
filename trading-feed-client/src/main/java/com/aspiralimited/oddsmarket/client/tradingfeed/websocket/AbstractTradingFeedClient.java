package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.ConnectionSelectionStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.ConnectionHealthManager;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.DefaultSessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.LowestIdHealthyConnectionStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.MessageProcessor;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractTradingFeedClient {
    private final TradingFeedConnectionManager tradingFeedConnectionManager;
    private final ConnectionHealthManager connectionHealthManager;

    public AbstractTradingFeedClient(List<String> hosts,
                                     TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig,
                                     TradingFeedListener tradingFeedListener,
                                     SessionRecoveryStrategy sessionRecoveryStrategy,
                                     ConnectionSelectionStrategy connectionSelectionStrategy,
                                     boolean json
    ) {
        if (connectionSelectionStrategy == null) {
            connectionSelectionStrategy = new LowestIdHealthyConnectionStrategy();
        }
        connectionHealthManager = new ConnectionHealthManager(connectionSelectionStrategy);
        MessageProcessor messageProcessor = new MessageProcessor(tradingFeedListener, connectionHealthManager);
        TradingFeedConnectionFactory tradingFeedConnectionFactory = new TradingFeedConnectionFactory(
                tradingFeedSubscriptionConfig,
                messageProcessor,
                connectionHealthManager,
                sessionRecoveryStrategy,
                json
        );
        tradingFeedConnectionManager = new TradingFeedConnectionManager(
                hosts,
                tradingFeedConnectionFactory,
                connectionHealthManager
        );
    }

    public void connect() throws WebSocketException, IOException, InterruptedException, ExecutionException {
        tradingFeedConnectionManager.establishNewSessionForAll();
    }

    public boolean isConnected() {
        TradingFeedConnection tradingFeedConnection = tradingFeedConnectionManager.getActiveTradingFeedConnection();
        if (tradingFeedConnection != null) {
            return tradingFeedConnection.isConnected();
        } else {
            return false;
        }
    }

    public void disconnect(boolean force) {
        tradingFeedConnectionManager.disconnectAll(force);
    }

    public void send(OddsmarketTradingDto.ClientMessage message) {
        tradingFeedConnectionManager.send(message);
    }

    public String getCurrentSessionId() {
        TradingFeedConnection tradingFeedConnection = tradingFeedConnectionManager.getActiveTradingFeedConnection();
        return tradingFeedConnection != null ? tradingFeedConnection.getSessionId() : null;
    }

    public int getActiveConnectionId() {
        return connectionHealthManager.getActiveConnectionId();
    }
}
