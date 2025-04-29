package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeed;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradingFeedDispatcherTest {


    @Test
    public void shouldReturnFirstActiveFeedWhenItIsHealthy() {
        TradingFeed feed1 = Mockito.mock(TradingFeed.class);
        TradingFeed feed2 = Mockito.mock(TradingFeed.class);
        Mockito.when(feed1.isHealthy()).thenReturn(true);
        Mockito.when(feed2.isHealthy()).thenReturn(true);

        List<TradingFeed> feeds = Arrays.asList(feed1, feed2);
        TradingFeedDispatcher dispatcher = new TradingFeedDispatcher(feeds, null);
        dispatcher.reevaluateActiveTradingFeed();

        TradingFeed activeFeed = dispatcher.getActiveTradingFeed();
        assertEquals(feed1, activeFeed);
    }

    @Test
    public void shouldReturnAnotherHealthyFeedWhenActiveFeedIsUnhealthy() {
        TradingFeed feed1 = Mockito.mock(TradingFeed.class);
        TradingFeed feed2 = Mockito.mock(TradingFeed.class);
        Mockito.when(feed1.isHealthy()).thenReturn(false);
        Mockito.when(feed2.isHealthy()).thenReturn(true);

        List<TradingFeed> feeds = Arrays.asList(feed1, feed2);
        TradingFeedDispatcher dispatcher = new TradingFeedDispatcher(feeds, null);
        TradingFeed activeFeed = dispatcher.getActiveTradingFeed();
        assertEquals(feed2, activeFeed);
    }

    @Test
    public void shouldReturnTrueWhenCheckedFeedIsCurrentlyActive() {
        TradingFeed feed1 = Mockito.mock(TradingFeed.class);
        TradingFeed feed2 = Mockito.mock(TradingFeed.class);
        Mockito.when(feed1.isHealthy()).thenReturn(true);

        List<TradingFeed> feeds = Arrays.asList(feed1, feed2);
        TradingFeedDispatcher dispatcher = new TradingFeedDispatcher(feeds, null);
        dispatcher.reevaluateActiveTradingFeed();

        assertTrue(dispatcher.isTradingFeedActive(feed1));
    }

    @Test
    public void shouldReturnFalseWhenCheckedFeedIsNotActive() {
        TradingFeed feed1 = Mockito.mock(TradingFeed.class);
        TradingFeed feed2 = Mockito.mock(TradingFeed.class);
        Mockito.when(feed1.isHealthy()).thenReturn(true);
        Mockito.when(feed2.isHealthy()).thenReturn(true);

        List<TradingFeed> feeds = Arrays.asList(feed1, feed2);
        TradingFeedDispatcher dispatcher = new TradingFeedDispatcher(feeds, null);
        dispatcher.reevaluateActiveTradingFeed();

        assertFalse(dispatcher.isTradingFeedActive(feed2));
    }

    @Test
    public void shouldReturnFalseWhenNoActiveFeedIsSet() {
        TradingFeed feed1 = Mockito.mock(TradingFeed.class);
        TradingFeed feed2 = Mockito.mock(TradingFeed.class);

        List<TradingFeed> feeds = Arrays.asList(feed1, feed2);
        TradingFeedDispatcher dispatcher = new TradingFeedDispatcher(feeds, null);

        assertFalse(dispatcher.isTradingFeedActive(feed1));
        assertFalse(dispatcher.isTradingFeedActive(feed2));
    }

}