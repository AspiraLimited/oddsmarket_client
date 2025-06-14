package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.integration;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedNewSessionParams;
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

public class TradingFeedClientIntegrationTest {

    private static final Duration DEFAULT_AWAIT_TIMEOUT = Duration.ofSeconds(5);
    private static final int PRIMARY_SERVER_PORT = 9090;
    private static final int FALLBACK_SERVER_PORT = 9091;
    private static TestTradingFeedWebsocketServer primaryServer = new TestTradingFeedWebsocketServer();
    private static TestTradingFeedWebsocketServer fallbackServer = new TestTradingFeedWebsocketServer();
    private static TradingFeedClient tradingFeedClient;
    private static TradingFeedStateKeepingListener tradingFeedStateKeepingListener;


    public TradingFeedClientIntegrationTest() throws WebSocketException, IOException, ExecutionException, InterruptedException {
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        primaryServer.start(PRIMARY_SERVER_PORT);
        fallbackServer.start(FALLBACK_SERVER_PORT);
        tradingFeedStateKeepingListener = new TradingFeedStateKeepingListener();
        tradingFeedClient = constructTradingFeedClient(tradingFeedStateKeepingListener);

    }

    private static TradingFeedClient constructTradingFeedClient(TradingFeedStateKeepingListener tradingFeedStateKeepingListener) throws WebSocketException, IOException, ExecutionException, InterruptedException {
        return TradingFeedClient.authenticateAndSubscribe(
                "ws://localhost:" + PRIMARY_SERVER_PORT,
                "ws://localhost:" + FALLBACK_SERVER_PORT,
                "TEST_API_KEY",
                (short) 1,
                tradingFeedStateKeepingListener,
                TradingFeedNewSessionParams.builder()
                        .resumeBufferLimitSeconds(15)
                        .build()
        );
    }

    @Test
    void shouldResumeSessionWhenConnectionDrops() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.stop();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
    }

    @Test
    void shouldSwitchToTheFallbackInstanceWhenConnectionDropsAndReconnectFails() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.stop();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedClient.getActiveFeedIndex());
        });
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(1, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
    }

    @Test
    void shouldResumeLostSessionWhenSwitchedToTheFallbackInstance() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.stop();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedClient.getActiveFeedIndex());
        });
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(1, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(3, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
    }

    @Test
    void shouldEstablishNewSessionWhenSwitchedToTheFallbackInstanceAndLostSessionIsFinallyLost() throws Exception {
        tradingFeedStateKeepingListener.clearStorage();
        primaryServer.sendHeartbeatMessageToClient();
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(1, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
            Assertions.assertEquals(0, tradingFeedClient.getActiveFeedIndex());
        });
        primaryServer.stop();
        Thread.sleep(16000);
        fallbackServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(2, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
        });
        primaryServer.start(PRIMARY_SERVER_PORT);
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertTrue(primaryServer.isClientConnected());
        });
        primaryServer.sendHeartbeatMessageToClient();
        Awaitility.await().atMost(DEFAULT_AWAIT_TIMEOUT).untilAsserted(() -> {
            Assertions.assertEquals(3, tradingFeedStateKeepingListener.getInMemoryStateStorage().getHeartbeats().size());
        });
    }

    @AfterAll
    static void afterAll() throws Exception {
        tradingFeedClient.disconnect();
        primaryServer.stop();
        fallbackServer.stop();
    }

}
