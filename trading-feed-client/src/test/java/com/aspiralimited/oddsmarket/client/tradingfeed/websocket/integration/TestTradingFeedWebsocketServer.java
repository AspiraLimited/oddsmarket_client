package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.integration;


import java.io.IOException;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

public class TestTradingFeedWebsocketServer {
    private Server server;
    private TestTradingFeedWebSocket testTradingFeedWebSocket;

    public void start(int port) throws Exception {
        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        JettyWebSocketServletContainerInitializer.configure(context, null);
        testTradingFeedWebSocket = new TestTradingFeedWebSocket();
        ServletHolder wsHolder = new ServletHolder("tradingFeed", new TestTradingFeedWebSocketServlet(testTradingFeedWebSocket));
        context.addServlet(wsHolder, "/trading/new_ws_session");
        context.addServlet(wsHolder, "/trading/resume_ws_session");
        server.start();
    }

    public void stop() throws Exception {
        if (server != null && server.isStarted()) {
            server.stop();
        }

    }

    public void sendServerMessageToClient(OddsmarketTradingDto.ServerMessage serverMessage) throws IOException {
        testTradingFeedWebSocket.sendMessageToClient(serverMessage);
    }

    public void sendHeartbeatMessageToClient() throws IOException {
        OddsmarketTradingDto.Heartbeat heartbeat = OddsmarketTradingDto.Heartbeat.newBuilder()
                .setServerTimestamp(System.currentTimeMillis())
                .build();
        OddsmarketTradingDto.ServerMessage serverMessage = OddsmarketTradingDto.ServerMessage.newBuilder()
                .setHeartbeat(heartbeat)
                .build();
        sendServerMessageToClient(serverMessage);
    }

    public boolean isClientConnected() {
        return testTradingFeedWebSocket.isClientConnected();
    }

}
