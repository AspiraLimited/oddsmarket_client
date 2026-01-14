package com.aspiralimited.oddsmarket.client.wsevents.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitialStateTransferredMessage.class, name = "INITIAL_STATE_TRANSFERRED"),
        @JsonSubTypes.Type(value = PingWebSocketMessage.class, name = "PING"),
        @JsonSubTypes.Type(value = PongWebSocketMessage.class, name = "PONG"),
        @JsonSubTypes.Type(value = FieldsMessage.class, name = "FIELDS"),
        @JsonSubTypes.Type(value = EventMessage.class, name = "EVENT"),
        @JsonSubTypes.Type(value = EventLiveInfoMessage.class, name = "EVENT_LIVE_INFO"),
        @JsonSubTypes.Type(value = EventDeletedMessage.class, name = "EVENT_DELETED"),
        @JsonSubTypes.Type(value = EventPlayersMessage.class, name = "EVENT_PLAYERS"),
})
public interface WebSocketMessage {
}
