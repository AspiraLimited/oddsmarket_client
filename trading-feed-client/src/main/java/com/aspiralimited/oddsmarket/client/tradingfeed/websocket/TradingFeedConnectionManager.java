package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl.ConnectionHealthManager;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionResult;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class TradingFeedConnectionManager {
    private final ConnectionHealthManager connectionHealthManager;
    private final List<TradingFeedConnection> tradingFeedConnections = new ArrayList<>();


    public TradingFeedConnectionManager(List<String> hosts, TradingFeedConnectionFactory tradingFeedConnectionFactory, ConnectionHealthManager connectionHealthManager) {
        this.connectionHealthManager = connectionHealthManager;
        for (String host : hosts) {
            TradingFeedConnection tradingFeedConnection = tradingFeedConnectionFactory.constructTradingFeedConnection(host);
            tradingFeedConnections.add(tradingFeedConnection);
        }
    }

    public void disconnectAll() {
        for (TradingFeedConnection feed : tradingFeedConnections) {
            feed.disconnect();
        }
    }

    public void establishNewSessionForAll() throws WebSocketException, IOException, InterruptedException, ExecutionException {
        for (TradingFeedConnection feed : tradingFeedConnections) {
            TradingFeedConnectionResult tradingFeedConnectionResult = feed.establishNewSession().get();
            if (tradingFeedConnectionResult.statusCode != TradingFeedConnectionStatusCode.SUCCESS) {
                throw new ConnectException(tradingFeedConnectionResult.message);
            }
        }
    }

    public void send(OddsmarketTradingDto.ClientMessage message) {
        TradingFeedConnection tradingFeedConnection = getActiveTradingFeedConnection();
        if (tradingFeedConnection != null) {
            tradingFeedConnection.send(message);
        }
    }

    public TradingFeedConnection getActiveTradingFeedConnection() {
        int activeConnectionId = connectionHealthManager.getActiveConnectionId();
        for (TradingFeedConnection tradingFeedConnection : tradingFeedConnections) {
            if (tradingFeedConnection.getConnectionId() == activeConnectionId) {
                return tradingFeedConnection;
            }
        }
        return null;
    }

}
