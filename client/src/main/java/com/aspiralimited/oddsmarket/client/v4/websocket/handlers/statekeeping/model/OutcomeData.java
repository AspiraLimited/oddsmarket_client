package com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OutcomeData {
    private final String id;
    private final long bookmakerEventId;
    private final short periodIdentifier;
    private final String periodName;
    private final short marketAndBetTypeId;
    private final float marketAndBetTypeParameterValue;
    private final Integer playerId1;
    private volatile String playerName1;
    private final Integer playerId2;
    private volatile String playerName2;
    private volatile float odds;
    private volatile Float oddsLay = null;
    private volatile Float marketDepth = null;
    private volatile String directLink = null;
    private volatile long updatedAt;

    public void updateProperties(OutcomeDto outcomeDto) {
        playerName1 = outcomeDto.playerName1;
        playerName2 = outcomeDto.playerName2;
        odds = outcomeDto.odds;
        oddsLay = outcomeDto.oddsLay;
        marketDepth = outcomeDto.marketDepth;
        directLink = outcomeDto.directLink;
        updatedAt = outcomeDto.updatedAt;
    }

}
