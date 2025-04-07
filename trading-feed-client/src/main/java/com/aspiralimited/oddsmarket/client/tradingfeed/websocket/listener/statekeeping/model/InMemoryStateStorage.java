package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.statekeeping.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStateStorage {
    @Getter
    private Map<Long, OddsmarketTradingDto.EventSnapshot> eventByEventId = new HashMap<>();

    public void addEvent(OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        eventByEventId.put(eventSnapshot.getEventId(), eventSnapshot);
    }

    public void updateEvent(OddsmarketTradingDto.EventPatch eventPatch) {
        if (eventByEventId.containsKey(eventPatch.getEventId())) {
            OddsmarketTradingDto.EventSnapshot eventSnapshot = mergeEventPatchToEventSnapshot(eventPatch, eventByEventId.get(eventPatch.getEventId()));
            eventByEventId.put(eventSnapshot.getEventId(), eventSnapshot);
        }
    }

    private OddsmarketTradingDto.EventSnapshot mergeEventPatchToEventSnapshot(OddsmarketTradingDto.EventPatch eventPatch, OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        //TODO implement!!!
        return OddsmarketTradingDto.EventSnapshot.newBuilder().build();
    }

    public void removeEvent(OddsmarketTradingDto.EventsRemoved eventsRemoved) {
        for (Long eventId : eventsRemoved.getEventIdsList()) {
            eventByEventId.remove(eventId);
        }
    }
}
