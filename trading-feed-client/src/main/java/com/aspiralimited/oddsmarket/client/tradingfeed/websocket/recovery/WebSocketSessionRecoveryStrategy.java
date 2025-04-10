package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.recovery;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.WebSocketClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.CurrentTradingFeedState;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.WebsocketConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WebSocketSessionRecoveryStrategy {
    @Getter
    private final CurrentTradingFeedState currentTradingFeedState;
    private final long connectionTimeout = 3000;
    private Integer resumeBufferLimitSeconds;
    private final WebSocketClient primaryWebSocketClient;
    private final WebSocketClient fallbackWebsocketClient;
    private final int resumeRetryInterval = 1000;
    private final int newSessionRetryInterval = 3000;

    public void executePrimarySessionRecovery() throws InterruptedException, IOException, WebSocketException {
        long disconnectTimestamp = System.currentTimeMillis();
        WebsocketConnectionStatusCode resumeSessionStatusCode = primaryWebSocketClient.resumeSession();
        if (resumeSessionStatusCode.isSuccess()) {
            return;
        }
        switchTradingFeedState(List.of(
                StateTransition.of(TradingFeedState.HEALTHY, TradingFeedState.FALLBACK),
                StateTransition.of(TradingFeedState.PRIMARY, TradingFeedState.UNHEALTHY)
        ));

        while (!resumeSessionStatusCode.isSuccess() && !resumeSessionStatusCode.is4xxxErrorCode() && System.currentTimeMillis() - disconnectTimestamp < resumeBufferLimitSeconds * 1000) {
            resumeSessionStatusCode = primaryWebSocketClient.resumeSession();
            Thread.sleep(resumeRetryInterval);
        }
        if (resumeSessionStatusCode.isSuccess()) {
            switchTradingFeedStateToHealthyOrPrimary();
            return;
        }
        WebsocketConnectionStatusCode newSessionStatusCode = primaryWebSocketClient.establishNewSession();
        if (newSessionStatusCode.isSuccess()) {
            switchTradingFeedStateToHealthyOrPrimary();
            return;
        }
        while (!newSessionStatusCode.isSuccess()) {
            newSessionStatusCode = primaryWebSocketClient.establishNewSession();
            Thread.sleep(newSessionRetryInterval);
        }
        switchTradingFeedStateToHealthyOrPrimary();


    }

    private void switchTradingFeedStateToHealthyOrPrimary() {
        switchTradingFeedState(List.of(
                StateTransition.of(TradingFeedState.FALLBACK, TradingFeedState.HEALTHY),
                StateTransition.of(TradingFeedState.UNHEALTHY, TradingFeedState.PRIMARY)
        ));
    }

    public void executeFallbackSessionRecovery() throws InterruptedException, WebSocketException, IOException {
        long disconnectTimestamp = System.currentTimeMillis();
        switchTradingFeedState(List.of(
                StateTransition.of(TradingFeedState.HEALTHY, TradingFeedState.PRIMARY),
                StateTransition.of(TradingFeedState.FALLBACK, TradingFeedState.UNHEALTHY)
        ));

        WebsocketConnectionStatusCode resumeSessionStatusCode = WebsocketConnectionStatusCode.UNDEFINED;
        while (!resumeSessionStatusCode.isSuccess() && !resumeSessionStatusCode.is4xxxErrorCode() && System.currentTimeMillis() - disconnectTimestamp < resumeBufferLimitSeconds * 1000) {
            resumeSessionStatusCode = fallbackWebsocketClient.resumeSession();
            Thread.sleep(resumeRetryInterval);
        }
        if (resumeSessionStatusCode.isSuccess()) {
            switchTradingFeedStateToHealthyOrFallback();
            return;
        }
        WebsocketConnectionStatusCode newSessionStatusCode = fallbackWebsocketClient.establishNewSession();
        if (newSessionStatusCode.isSuccess()) {
            switchTradingFeedStateToHealthyOrFallback();
            return;
        }
        while (!newSessionStatusCode.isSuccess()) {
            newSessionStatusCode = fallbackWebsocketClient.establishNewSession();
            Thread.sleep(newSessionRetryInterval);
        }
        switchTradingFeedStateToHealthyOrFallback();
    }

    private void switchTradingFeedStateToHealthyOrFallback() {
        switchTradingFeedState(List.of(
                StateTransition.of(TradingFeedState.PRIMARY, TradingFeedState.HEALTHY),
                StateTransition.of(TradingFeedState.UNHEALTHY, TradingFeedState.FALLBACK)
        ));
    }

    private void switchTradingFeedState(List<StateTransition> stateTransitions) {
        Set<TradingFeedState> finallyTradingFeedStateSet = stateTransitions.stream()
                .map(StateTransition::getTo)
                .collect(Collectors.toSet());
        while (!finallyTradingFeedStateSet.contains(currentTradingFeedState.get())) {
            for (StateTransition stateTransition : stateTransitions) {
                currentTradingFeedState.compareAndSet(stateTransition.from, stateTransition.to);
            }
        }
    }

    @RequiredArgsConstructor
    static class StateTransition {
        private final TradingFeedState from;
        @Getter
        private final TradingFeedState to;

        public static StateTransition of(TradingFeedState from, TradingFeedState to) {
            return new StateTransition(from, to);
        }

    }


}
