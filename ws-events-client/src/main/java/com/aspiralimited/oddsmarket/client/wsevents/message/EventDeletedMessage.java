package com.aspiralimited.oddsmarket.client.wsevents.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EventDeletedMessage implements WebSocketMessage {

    @JsonProperty("data")
    long eventId;
}
