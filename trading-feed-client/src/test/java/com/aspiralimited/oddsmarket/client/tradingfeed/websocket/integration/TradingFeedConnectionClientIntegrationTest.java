package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.integration;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedMulticonnectionClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedSubscriptionConfig;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.SessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.DefaultSessionRecoveryStrategy;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.TradingFeedStateKeepingListener;
import com.neovisionaries.ws.client.WebSocketException;

import org.awaitility.Awaitility;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class TradingFeedConnectionClientIntegrationTest {

    private static final Duration DEFAULT_AWAIT_TIMEOUT = Duration.ofSeconds(5);
    private static final int PRIMARY_SERVER_PORT = 9090;
    private static final int FALLBACK_SERVER_PORT = 9091;
    private static TestTradingFeedWebsocketServer primaryServer = new TestTradingFeedWebsocketServer();
    private static TestTradingFeedWebsocketServer fallbackServer = new TestTradingFeedWebsocketServer();
    private static TradingFeedMulticonnectionClient tradingFeedMulticonnectionClient;
    private static TradingFeedStateKeepingListener tradingFeedStateKeepingListener;
    private static AtomicInteger heartbeatCounter = new AtomicInteger();


    public TradingFeedConnectionClientIntegrationTest() throws WebSocketException, IOException, ExecutionException, InterruptedException {
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        primaryServer.start(PRIMARY_SERVER_PORT);
        fallbackServer.start(FALLBACK_SERVER_PORT);
        tradingFeedStateKeepingListener = new TradingFeedStateKeepingListener() {
            @Override
            public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
                super.onServerMessage(serverMessage);
                if (serverMessage.getPayloadCase() == OddsmarketTradingDto.ServerMessage.PayloadCase.HEARTBEAT) {
                    heartbeatCounter.incrementAndGet();
                }
            }
        };
        tradingFeedMulticonnectionClient = constructTradingFeedClient(tradingFeedStateKeepingListener);
        tradingFeedMulticonnectionClient.connect();

    }

    private static TradingFeedMulticonnectionClient constructTradingFeedClient(TradingFeedStateKeepingListener tradingFeedStateKeepingListener) throws WebSocketException, IOException, ExecutionException, InterruptedException {
        return TradingFeedMulticonnectionClient.builder()
                .primaryHost("ws://localhost:" + PRIMARY_SERVER_PORT)
                .fallbackHost("ws://localhost:" + FALLBACK_SERVER_PORT)
                .tradingFeedSubscriptionConfig(TradingFeedSubscriptionConfig.builder()
                        .apiKey("TEST_API_KEY")
                        .tradingFeedId((short) 1)
                        .build())
                .tradingFeedListener(tradingFeedStateKeepingListener)
                .build();

    }

    @Test
    void shouldResumeSessionWhenConnectionDrops() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        heartbeatCounter.set(0);
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, heartbeatCounter.get());
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.stop();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, heartbeatCounter.get());
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
    }

    @Test
    void shouldSwitchToTheFallbackInstanceWhenConnectionDropsAndReconnectFails() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        heartbeatCounter.set(0);
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, heartbeatCounter.get());
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.stop();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, heartbeatCounter.get());
            Assertions.assertEquals(2, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
    }

    @Test
    void shouldResumeLostSessionWhenSwitchedToTheFallbackInstance() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, heartbeatCounter.get());
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.stop();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, heartbeatCounter.get());
            Assertions.assertEquals(2, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(3, heartbeatCounter.get());
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
    }

    @Test
    void shouldEstablishNewSessionWhenSwitchedToTheFallbackInstanceAndLostSessionIsFinallyLost() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        heartbeatCounter.set(0);
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, heartbeatCounter.get());
            Assertions.assertEquals(1, tradingFeedMulticonnectionClient.getActiveConnectionId());
        });
        primaryServer.stop();
        Thread.sleep(16000);
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, heartbeatCounter.get());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertTrue(primaryServer.isClientConnected());
        });
        primaryServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(3, heartbeatCounter.get());
        });
    }

    @AfterAll
    static void afterAll() throws Exception {
        tradingFeedMulticonnectionClient.disconnect();
        primaryServer.stop();
        fallbackServer.stop();
    }

}
