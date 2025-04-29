package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.impl.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import lombok.Getter;

import java.util.*;

@Getter
public class InMemoryStateStorage {
    private final Map<Long, OddsmarketTradingDto.EventSnapshot> eventByEventId = new HashMap<>();
    private final List<OddsmarketTradingDto.Heartbeat> heartbeats = new ArrayList<>();

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
        OddsmarketTradingDto.EventSnapshot.Builder resultBuilder = OddsmarketTradingDto.EventSnapshot.newBuilder()
                .setEventId(eventSnapshot.getEventId());
        if (eventPatch.hasUpdatedEventMetadata()) {
            resultBuilder.setEventMetadata(eventPatch.getUpdatedEventMetadata());
        } else {
            resultBuilder.setEventMetadata(eventSnapshot.getEventMetadata());
        }
        if (eventPatch.hasUpdatedLiveEventInfo()) {
            resultBuilder.setLiveEventInfo(eventPatch.getUpdatedLiveEventInfo());
        } else if (eventSnapshot.hasLiveEventInfo()) {
            resultBuilder.setLiveEventInfo(eventSnapshot.getLiveEventInfo());
        }
        if (eventPatch.getUpdatedMarketsCount() > 0) {
            Set<OddsmarketTradingDto.MarketSnapshot> mergedMarketSnapshots = new HashSet<>();
            mergedMarketSnapshots.addAll(eventSnapshot.getMarketsList());
            mergedMarketSnapshots.addAll(eventPatch.getUpdatedMarketsList());
            resultBuilder.addAllMarkets(mergedMarketSnapshots);
        }
        return resultBuilder.build();
    }

    public void removeEvent(OddsmarketTradingDto.EventsRemoved eventsRemoved) {
        for (Long eventId : eventsRemoved.getEventIdsList()) {
            eventByEventId.remove(eventId);
        }
    }

    public void addHeartbeat(OddsmarketTradingDto.Heartbeat heartbeat) {
        heartbeats.add(heartbeat);
    }

    public void clearAll() {
        heartbeats.clear();
        eventByEventId.clear();
    }
}
