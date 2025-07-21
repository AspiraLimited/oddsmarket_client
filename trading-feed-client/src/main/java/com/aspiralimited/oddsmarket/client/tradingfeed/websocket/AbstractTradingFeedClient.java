package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.ConnectionHealthManager;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.DefaultSessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.MessageProcessor;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.Node;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractTradingFeedClient {
    private final TradingFeedConnectionManager tradingFeedConnectionManager;
    private final ConnectionHealthManager connectionHealthManager;
    private final static int MAX_PREMATCH_RESUME_BUFFER_LIMIT_SECONDS = 240;
    private final static int MAX_LIVE_RESUME_BUFFER_LIMIT_SECONDS = 60;

    public AbstractTradingFeedClient(List<String> hosts,
                                     TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig,
                                     TradingFeedListener tradingFeedListener,
                                     boolean autoReconnect,
                                     int maxReconnectAttempts,
                                     Integer resumeBufferLimitSeconds
    ) {

        connectionHealthManager = new ConnectionHealthManager();
        validateResumeBufferLimitSeconds(resumeBufferLimitSeconds, hosts.get(0));
        SessionRecoveryStrategy sessionRecoveryStrategy = new DefaultSessionRecoveryStrategy(autoReconnect, resumeBufferLimitSeconds, maxReconnectAttempts);
        MessageProcessor messageProcessor = new MessageProcessor(tradingFeedListener, connectionHealthManager);
        TradingFeedConnectionFactory tradingFeedConnectionFactory = new TradingFeedConnectionFactory(
                tradingFeedSubscriptionConfig,
                messageProcessor,
                connectionHealthManager,
                sessionRecoveryStrategy
        );
        tradingFeedConnectionManager = new TradingFeedConnectionManager(
                hosts,
                tradingFeedConnectionFactory,
                connectionHealthManager
        );
    }

    private void validateResumeBufferLimitSeconds(Integer resumeBufferLimitSeconds, String host) {
        if (resumeBufferLimitSeconds == null) {
            return;
        }
        Node node = Node.parse(host);
        if (node == Node.PREMATCH) {
            if (resumeBufferLimitSeconds < 0 || resumeBufferLimitSeconds > MAX_PREMATCH_RESUME_BUFFER_LIMIT_SECONDS) {
                throw new IllegalStateException("ResumeBufferLimitSeconds must be from 0 to " + MAX_PREMATCH_RESUME_BUFFER_LIMIT_SECONDS + " seconds");
            }
        } else {
            if (resumeBufferLimitSeconds < 0 || resumeBufferLimitSeconds > MAX_LIVE_RESUME_BUFFER_LIMIT_SECONDS) {
                throw new IllegalStateException("ResumeBufferLimitSeconds must be from 0 to " + MAX_LIVE_RESUME_BUFFER_LIMIT_SECONDS + " seconds");
            }

        }
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

    public void disconnect() {
        tradingFeedConnectionManager.disconnectAll();
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
