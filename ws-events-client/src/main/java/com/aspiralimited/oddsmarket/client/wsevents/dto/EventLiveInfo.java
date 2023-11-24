package com.aspiralimited.oddsmarket.client.wsevents.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EventLiveInfo {

    long eventId;
    short status;
    int minute;
    String score;
    String cornersScore;
    String redCardsScore;
    String yellowCardsScore;
    short periodIdentifier;
    long scoreModifiedDateTime;
    long modifiedDateTime;
}
