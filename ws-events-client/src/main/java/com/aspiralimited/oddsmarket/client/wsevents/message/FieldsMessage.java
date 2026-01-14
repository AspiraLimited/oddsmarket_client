package com.aspiralimited.oddsmarket.client.wsevents.message;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class FieldsMessage implements WebSocketMessage {

    Data data;

    @Value
    @Builder
    @Jacksonized
    public static class Data {

        List<String> event;

        List<String> eventLiveInfo;

        List<String> eventPlayers;

        List<String> eventPlayer;
    }
}
