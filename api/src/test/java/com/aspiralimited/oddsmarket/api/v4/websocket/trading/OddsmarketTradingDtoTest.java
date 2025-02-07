package com.aspiralimited.oddsmarket.api.v4.websocket.trading;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OddsmarketTradingDtoTest {
    @Test
    public void testAcknowledgeConstruction() {
        // given
        OddsmarketTradingDto.ClientMessage ackMsg = OddsmarketTradingDto.ClientMessage.newBuilder()
                .setAcknowledge(
                        OddsmarketTradingDto.Acknowledge.newBuilder()
                                .setMessageId(1L)
                                .build()
                )
                .build();

        // when
        String mstString = ackMsg.toString();

        // then
        assertEquals("acknowledge {\n" + //
                        "  messageId: 1\n" + //
                        "}\n", mstString);
    }

    @Test
    public void testPingConstruction() {
        // given
        OddsmarketTradingDto.ClientMessage pingMsg = OddsmarketTradingDto.ClientMessage.newBuilder()
                .setPing(
                        OddsmarketTradingDto.Ping.newBuilder()
                                .setPayload("test-ping-1")
                                .build()
                )
                .build();

        // when
        String msgString = pingMsg.toString();

        // then
        assertEquals("ping {\n" + //
                        "  payload: \"test-ping-1\"\n" + //
                        "}\n", msgString);
    }

    @Test
    public void testCannotSetBothAcknowledgeAndPing() {
        // given
        OddsmarketTradingDto.ClientMessage.Builder builder = OddsmarketTradingDto.ClientMessage.newBuilder()
                .setAcknowledge(
                        OddsmarketTradingDto.Acknowledge.newBuilder()
                                .setMessageId(1L)
                                .build()
                );

        // when/then
        // Setting ping after acknowledge should clear acknowledge due to oneof message field
        builder.setPing(
                OddsmarketTradingDto.Ping.newBuilder()
                        .setPayload("test-ping-1")
                        .build()
        );
        
        OddsmarketTradingDto.ClientMessage msg = builder.build();
        assertTrue(msg.hasPing(), "Message should have ping");
        assertFalse(msg.hasAcknowledge(), "Message should not have acknowledge");
    }

}
