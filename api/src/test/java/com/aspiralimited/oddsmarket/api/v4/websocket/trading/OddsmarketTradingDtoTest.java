package com.aspiralimited.oddsmarket.api.v4.websocket.trading;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class OddsmarketTradingDtoTest {
    @Test
    public void testAcknowledgeConstruction() {
        // given
        OddsmarketTradingDto.ClientMessage ackMsg = OddsmarketTradingDto.ClientMessage.newBuilder()
                .setAck(
                        OddsmarketTradingDto.Ack.newBuilder()
                                .setMessageId(1L)
                                .build()
                )
                .build();

        // when
        String mstString = ackMsg.toString();

        // then
        assertEquals("ack {\n" + //
                "  messageId: 1\n" + //
                "}\n", mstString);
    }
}
