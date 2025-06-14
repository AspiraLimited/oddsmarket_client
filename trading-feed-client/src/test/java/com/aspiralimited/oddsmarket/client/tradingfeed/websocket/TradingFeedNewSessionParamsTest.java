package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TradingFeedNewSessionParamsTest {

    @Test
    public void shouldReturnQueryStringWithAllParametersWhenAllFieldsAreSet() {
        TradingFeedNewSessionParams params = TradingFeedNewSessionParams.builder()
                .sportIds(Set.of((short) 1))
                .locales(Set.of("en"))
                .rawIdOriginBookmakerId((short) 100)
                .fillRawOutcomeId(true)
                .resumeBufferLimitSeconds(60)
                .json(true)
                .resumeRetryInterval(5)
                .newSessionRetryInterval(10)
                .build();

        String expectedQueryString = "&sportIds=1&locales=en&rawIdOriginBookmakerId=100&fillRawOutcomeId=true&resumeBufferLimitSeconds=60&json=true";
        assertEquals(expectedQueryString, params.toQueryString());
    }

}