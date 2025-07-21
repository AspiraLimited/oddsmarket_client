package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DefaultSessionRecoveryStrategy implements SessionRecoveryStrategy {

    private final boolean autoReconnect;
    private final Integer resumeBufferLimitSeconds;
    private final Integer maxReconnectAttempts;
    private int resumeRetryInterval = 1000;
    private int newSessionRetryInterval = 3000;

    public DefaultSessionRecoveryStrategy(boolean autoReconnect, Integer resumeBufferLimitSeconds, Integer maxReconnectAttempts) {
        this.autoReconnect = autoReconnect;
        this.resumeBufferLimitSeconds = resumeBufferLimitSeconds;
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    @Override
    public void recover(TradingFeedConnection connection) throws IOException, WebSocketException, InterruptedException, ExecutionException, TimeoutException {
        if (!autoReconnect) {
            return;
        }
        long disconnectTimestamp = System.currentTimeMillis();
        TradingFeedConnectionStatusCode resumeSessionStatusCode = connection.resumeSession().get(resumeRetryInterval, TimeUnit.MILLISECONDS).statusCode;
        if (resumeSessionStatusCode.isSuccess()) {
            return;
        }
        while (!resumeSessionStatusCode.isSuccess() && !resumeSessionStatusCode.is4xxxErrorCode() && System.currentTimeMillis() - disconnectTimestamp < resumeBufferLimitSeconds * 1000) {
            resumeSessionStatusCode = connection.resumeSession().get(resumeRetryInterval, TimeUnit.MILLISECONDS).statusCode;
            if (resumeSessionStatusCode == TradingFeedConnectionStatusCode.CONNECTION_FAILED) {
                Thread.sleep(resumeRetryInterval);
            }
        }
        if (resumeSessionStatusCode.isSuccess()) {
            return;
        }
        TradingFeedConnectionStatusCode newSessionStatusCode = null;
        int attempts = 0;
        while (attempts < maxReconnectAttempts && (newSessionStatusCode == null || !newSessionStatusCode.isSuccess())) {
            newSessionStatusCode = connection.establishNewSession().get(newSessionRetryInterval, TimeUnit.MILLISECONDS).statusCode;
            attempts++;
        }
        throw new IllegalStateException("Failed to reconnect after " + attempts + " attempts");
    }
}
