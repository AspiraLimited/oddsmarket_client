package com.aspiralimited.oddsmarket.client.wsevents.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EventPlayer {

    int id;
    String name;
    Boolean isHome;
}
