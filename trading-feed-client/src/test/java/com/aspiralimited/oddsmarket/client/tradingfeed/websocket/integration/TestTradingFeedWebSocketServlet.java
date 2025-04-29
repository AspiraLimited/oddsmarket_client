package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.integration;

import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class TestTradingFeedWebSocketServlet extends JettyWebSocketServlet {
    private final TestTradingFeedWebSocket testTradingFeedWebSocket;

    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        factory.setCreator((req, resp) -> {
            String path = req.getRequestPath();
            testTradingFeedWebSocket.setPath(path);
            return testTradingFeedWebSocket;
        });
        factory.setIdleTimeout(Duration.of(120, ChronoUnit.SECONDS));
    }
}
