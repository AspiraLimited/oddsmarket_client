package com.aspiralimited.oddsmarket.client.wsevents.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    long id;
    String name;
    Integer homeId;
    String homeName;
    Integer awayId;
    String awayName;
    short sportId;
    int leagueId;
    String leagueName;
    long startedAt;
    long uniformEventId;
}
