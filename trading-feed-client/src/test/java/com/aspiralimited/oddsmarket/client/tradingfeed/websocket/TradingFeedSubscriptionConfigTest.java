package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class TradingFeedSubscriptionConfigTest {

    @Test
    public void shouldReturnQueryStringWithAllParametersWhenAllFieldsAreSet() {
        TradingFeedSubscriptionConfig params = TradingFeedSubscriptionConfig.builder()
                .apiKey("absde")
                .tradingFeedId((short) 7)
                .sportIds(Set.of((short) 1))
                .locales(Set.of("en"))
                .rawIdOriginBookmakerId((short) 100)
                .fillRawOutcomeId(true)
                .build();

        String expectedQueryString = "apiKey=absde&tradingFeedId=7&sportIds=1&locales=en&rawIdOriginBookmakerId=100&fillRawOutcomeId=true";
        Assertions.assertEquals(expectedQueryString, params.toQueryString());
    }


}