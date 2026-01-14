package com.aspiralimited.oddsmarket.client.wsevents.message;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EventPlayersMessage implements WebSocketMessage {

    JsonNode data;
}
