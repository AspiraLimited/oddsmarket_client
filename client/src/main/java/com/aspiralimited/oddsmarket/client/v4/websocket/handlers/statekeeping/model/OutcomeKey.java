package com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class OutcomeKey {
    private final short marketAndBetTypeId;
    private final float marketAndBetTypeParamValue;
    private final short periodIdentifier;
    private final boolean isLay;
    private final int playerId1;
    private final int playerId2;

    public OutcomeKey(OutcomeDto outcomeDto) {
        this.marketAndBetTypeId = outcomeDto.marketAndBetTypeId;
        this.marketAndBetTypeParamValue = outcomeDto.marketAndBetTypeParameterValue;
        this.periodIdentifier = outcomeDto.periodIdentifier;
        this.isLay = outcomeDto.isLay;
        this.playerId1 = outcomeDto.playerId1 == null ? 0 : outcomeDto.playerId1;
        this.playerId2 = outcomeDto.playerId2 == null ? 0 : outcomeDto.playerId2;
    }
}
