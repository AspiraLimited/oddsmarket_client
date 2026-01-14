package com.aspiralimited.oddsmarket.client.wsevents.message;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InitialStateTransferredMessage implements WebSocketMessage {
}
