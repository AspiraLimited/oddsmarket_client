package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.integration;


import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import lombok.Setter;
import lombok.SneakyThrows;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TestTradingFeedWebSocket implements WebSocketListener {
    @Setter
    private String path;
    private Session outbound;

    public void sendMessageToClient(OddsmarketTradingDto.ServerMessage serverMessage) throws IOException {
        outbound.getRemote().sendBytes(ByteBuffer.wrap(serverMessage.toByteArray()));
    }

    public void onWebSocketClose(int statusCode, String reason) {
        this.outbound = null;
    }

    @SneakyThrows
    public void onWebSocketConnect(Session session) {
        this.outbound = session;
        if (path.equals("/trading/new_ws_session")) {
            OddsmarketTradingDto.SessionStart sessionStart = OddsmarketTradingDto.SessionStart
                    .newBuilder()
                    .setSessionId("SESSION_ID")
                    .build();
            OddsmarketTradingDto.ServerMessage serverMessage = OddsmarketTradingDto.ServerMessage.newBuilder()
                    .setSessionStart(sessionStart)
                    .build();
            sendMessageToClient(serverMessage);
        }
    }

    public boolean isClientConnected() {
        return outbound != null;
    }
}
