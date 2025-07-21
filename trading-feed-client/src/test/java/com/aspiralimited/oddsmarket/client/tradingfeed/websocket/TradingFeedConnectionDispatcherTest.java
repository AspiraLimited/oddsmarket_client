package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.core.TradingFeedConnection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradingFeedConnectionDispatcherTest {


//    @Test
//    public void shouldReturnFirstActiveFeedWhenItIsHealthy() {
//        TradingFeedConnection feed1 = Mockito.mock(TradingFeedConnection.class);
//        TradingFeedConnection feed2 = Mockito.mock(TradingFeedConnection.class);
//        Mockito.when(feed1.isHealthy()).thenReturn(true);
//        Mockito.when(feed2.isHealthy()).thenReturn(true);
//
//        List<TradingFeedConnection> feeds = Arrays.asList(feed1, feed2);
//        TradingFeedConnectionManager dispatcher = new TradingFeedConnectionManager(feeds, null,null);
//        dispatcher.reevaluateActiveTradingFeedConnection();
//
//        TradingFeedConnection activeFeed = dispatcher.getActiveTradingFeedConnection();
//        assertEquals(feed1, activeFeed);
//    }
//
//    @Test
//    public void shouldReturnAnotherHealthyFeedWhenActiveFeedIsUnhealthy() {
//        TradingFeedConnection feed1 = Mockito.mock(TradingFeedConnection.class);
//        TradingFeedConnection feed2 = Mockito.mock(TradingFeedConnection.class);
//        Mockito.when(feed1.isHealthy()).thenReturn(false);
//        Mockito.when(feed2.isHealthy()).thenReturn(true);
//
//        List<TradingFeedConnection> feeds = Arrays.asList(feed1, feed2);
//        TradingFeedConnectionManager dispatcher = new TradingFeedConnectionManager(feeds, null,null);
//        TradingFeedConnection activeFeed = dispatcher.getActiveTradingFeedConnection();
//        assertEquals(feed2, activeFeed);
//    }
//
//    @Test
//    public void shouldReturnTrueWhenCheckedFeedIsCurrentlyActive() {
//        TradingFeedConnection feed1 = Mockito.mock(TradingFeedConnection.class);
//        TradingFeedConnection feed2 = Mockito.mock(TradingFeedConnection.class);
//        Mockito.when(feed1.isHealthy()).thenReturn(true);
//
//        List<TradingFeedConnection> feeds = Arrays.asList(feed1, feed2);
//        TradingFeedConnectionManager dispatcher = new TradingFeedConnectionManager(feeds, null, null);
//        dispatcher.reevaluateActiveTradingFeedConnection();
//
//        assertTrue(dispatcher.isTradingFeedActive(feed1));
//    }
//
//    @Test
//    public void shouldReturnFalseWhenCheckedFeedIsNotActive() {
//        TradingFeedConnection feed1 = Mockito.mock(TradingFeedConnection.class);
//        TradingFeedConnection feed2 = Mockito.mock(TradingFeedConnection.class);
//        Mockito.when(feed1.isHealthy()).thenReturn(true);
//        Mockito.when(feed2.isHealthy()).thenReturn(true);
//
//        List<TradingFeedConnection> feeds = Arrays.asList(feed1, feed2);
//        TradingFeedConnectionManager dispatcher = new TradingFeedConnectionManager(feeds, null, null);
//        dispatcher.reevaluateActiveTradingFeedConnection();
//
//        assertFalse(dispatcher.isTradingFeedActive(feed2));
//    }
//
//    @Test
//    public void shouldReturnFalseWhenNoActiveFeedIsSet() {
//        TradingFeedConnection feed1 = Mockito.mock(TradingFeedConnection.class);
//        TradingFeedConnection feed2 = Mockito.mock(TradingFeedConnection.class);
//
//        List<TradingFeedConnection> feeds = Arrays.asList(feed1, feed2);
//        TradingFeedConnectionManager dispatcher = new TradingFeedConnectionManager(feeds, null, null);
//
//        assertFalse(dispatcher.isTradingFeedActive(feed1));
//        assertFalse(dispatcher.isTradingFeedActive(feed2));
//    }

}