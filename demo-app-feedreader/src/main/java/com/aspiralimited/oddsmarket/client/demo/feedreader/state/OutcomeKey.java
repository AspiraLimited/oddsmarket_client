package com.aspiralimited.oddsmarket.client.demo.feedreader.state;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class OutcomeKey {
    private final short marketAndBetTypeId;
    private final float marketAndBetTypeParam;
    private final short periodIdentifier;
    private final boolean isLay;
    private final int playerId1;
    private final int playerId2;
}
