package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.impl;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedState;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LowestIdHealthyConnectionStrategyTest {


    @Test
    public void shouldReturnFirstActiveConnectionWhenItIsHealthy() {
        TradingFeedConnection connection1 = Mockito.mock(TradingFeedConnection.class);
        TradingFeedConnection connection2 = Mockito.mock(TradingFeedConnection.class);
        Mockito.when(connection1.getConnectionId()).thenReturn(1);
        Mockito.when(connection2.getConnectionId()).thenReturn(2);
        Map<Integer, TradingFeedState> tradingFeedStateByConnectionId = Map.of(
                connection1.getConnectionId(), TradingFeedState.HEALTHY,
                connection2.getConnectionId(), TradingFeedState.HEALTHY
        );
        LowestIdHealthyConnectionStrategy connectionSelectionStrategy = new LowestIdHealthyConnectionStrategy();
        int activeConnectionId = connectionSelectionStrategy.selectActiveConnection(tradingFeedStateByConnectionId);
        assertEquals(connection1.getConnectionId(), activeConnectionId);
    }

    @Test
    public void shouldReturnAnotherHealthyConnectionWhenActiveConnectionIsUnhealthy() {
        TradingFeedConnection connection1 = Mockito.mock(TradingFeedConnection.class);
        TradingFeedConnection connection2 = Mockito.mock(TradingFeedConnection.class);
        Mockito.when(connection1.getConnectionId()).thenReturn(1);
        Mockito.when(connection2.getConnectionId()).thenReturn(2);
        Map<Integer, TradingFeedState> tradingFeedStateByConnectionId = Map.of(
                connection1.getConnectionId(), TradingFeedState.UNHEALTHY,
                connection2.getConnectionId(), TradingFeedState.HEALTHY
        );
        LowestIdHealthyConnectionStrategy connectionSelectionStrategy = new LowestIdHealthyConnectionStrategy();
        int activeConnectionId = connectionSelectionStrategy.selectActiveConnection(tradingFeedStateByConnectionId);
        assertEquals(connection2.getConnectionId(), activeConnectionId);
    }

    @Test
    public void shouldReturn_1WhenNoActiveConnectionPresents() {
        TradingFeedConnection connection1 = Mockito.mock(TradingFeedConnection.class);
        TradingFeedConnection connection2 = Mockito.mock(TradingFeedConnection.class);
        Mockito.when(connection1.getConnectionId()).thenReturn(1);
        Mockito.when(connection2.getConnectionId()).thenReturn(2);
        Map<Integer, TradingFeedState> tradingFeedStateByConnectionId = Map.of(
                connection1.getConnectionId(), TradingFeedState.UNHEALTHY,
                connection2.getConnectionId(), TradingFeedState.UNHEALTHY
        );
        LowestIdHealthyConnectionStrategy connectionSelectionStrategy = new LowestIdHealthyConnectionStrategy();
        int activeConnectionId = connectionSelectionStrategy.selectActiveConnection(tradingFeedStateByConnectionId);
        assertEquals(-1, activeConnectionId);
    }
}