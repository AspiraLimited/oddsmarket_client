package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;

@Getter
public class InMemoryStateStorage {
    private final Map<Long, Event> eventByEventId = new HashMap<>();

    public void addEvent(OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        eventByEventId.put(eventSnapshot.getEventId(), protobufEventToEvent(eventSnapshot));
    }

    private Event protobufEventToEvent(OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        OddsmarketTradingDto.EventMetadata eventMetadata = eventSnapshot.getEventMetadata();
        return new Event(
                eventSnapshot.getEventId(),
                (short) eventMetadata.getSportId(),
                eventMetadata.getPlannedStartTimestamp(),
                extractEventName(eventMetadata),
                protobufMarketsToOutcomeMap(eventSnapshot.getMarketsList()),
                protobufLiveEventInfoToLiveEventInfo(eventSnapshot.getLiveEventInfo())
        );
    }

    private String extractEventName(OddsmarketTradingDto.EventMetadata eventMetadata) {
        String result = null;
        if (eventMetadata.getNamesCount() > 0) {
            result = eventMetadata.getNames(0).getName();
        }
        return result;
    }

    public LiveEventInfo protobufLiveEventInfoToLiveEventInfo(OddsmarketTradingDto.LiveEventInfo protobufLiveEventInfo) {
        if (protobufLiveEventInfo == null) {
            return null;
        }
        String mainScore = extractMainScore(protobufLiveEventInfo);
        return new LiveEventInfo(mainScore);
    }

    private String extractMainScore(OddsmarketTradingDto.LiveEventInfo protobufLiveEventInfo) {
        for (OddsmarketTradingDto.DetailedScore detailedScore : protobufLiveEventInfo.getDetailedScoresList()) {
            if (detailedScore.getScoreKey().getScoreType() == OddsmarketTradingDto.ScoreType.MAIN) {
                return detailedScore.getHome() + ":" + detailedScore.getAway();
            }
        }
        return null;
    }

    private Map<OutcomeKey, OutcomeData> protobufMarketsToOutcomeMap(List<OddsmarketTradingDto.MarketSnapshot> marketSnapshots) {
        Map<OutcomeKey, OutcomeData> result = new HashMap<>();
        for (OddsmarketTradingDto.MarketSnapshot marketSnapshot : marketSnapshots) {
            for (OddsmarketTradingDto.OutcomeSnapshot outcomeSnapshot : marketSnapshot.getOutcomesList()) {
                OutcomeKey outcomeKey = protobufOutcomeKeyToOutcomeKey(outcomeSnapshot.getOutcomeKey());
                OutcomeData outcomeData = protobufOutcomeDataToOutcomeData(outcomeSnapshot.getOutcomeData());
                result.put(outcomeKey, outcomeData);
            }
        }
        return result;
    }

    private OutcomeKey protobufOutcomeKeyToOutcomeKey(OddsmarketTradingDto.OutcomeKey protobufOutcomeKey) {
        return new OutcomeKey(
                (short) protobufOutcomeKey.getMarketAndBetTypeId(),
                protobufOutcomeKey.getMarketAndBetTypeParam(),
                (short) protobufOutcomeKey.getPeriodIdentifier(),
                protobufOutcomeKey.getPlayerId1(),
                protobufOutcomeKey.getPlayerId2()
        );
    }

    public OutcomeData protobufOutcomeDataToOutcomeData(OddsmarketTradingDto.OutcomeData protobufOutcomeData) {
        return new OutcomeData(
                protobufOutcomeData.getOdds()
        );
    }

    public Event getEvent(long eventId) {
        return eventByEventId.get(eventId);
    }


    public void updateEvent(OddsmarketTradingDto.EventPatch eventPatch) {
        if (eventByEventId.containsKey(eventPatch.getEventId())) {
            Event event = eventByEventId.get(eventPatch.getEventId());
            mergeEventPatchToEvent(eventPatch, event);
        }
    }

    private void mergeEventPatchToEvent(OddsmarketTradingDto.EventPatch eventPatch, Event event) {
        Event currentEvent = eventByEventId.get(eventPatch.getEventId());
        if (eventPatch.hasUpdatedLiveEventInfo()) {
            OddsmarketTradingDto.LiveEventInfo liveEventInfo = eventPatch.getUpdatedLiveEventInfo();
            String mainScore = extractMainScore(liveEventInfo);
            if (mainScore != null) {
                event.getLiveEventInfo().score = mainScore;
            }
        }
        if (eventPatch.getUpdatedMarketsCount() > 0) {
            Map<OutcomeKey, OutcomeData> updatedOutcomeMap = protobufMarketsToOutcomeMap(eventPatch.getUpdatedMarketsList());
            currentEvent.outcomeMap.putAll(updatedOutcomeMap);
        }
    }

    public boolean hasEvent(long eventId) {
        return eventByEventId.containsKey(eventId);
    }

    public void removeEvent(OddsmarketTradingDto.EventsRemoved eventsRemoved) {
        for (Long eventId : eventsRemoved.getEventIdsList()) {
            eventByEventId.remove(eventId);
        }
    }

    public void clearAll() {
        eventByEventId.clear();
    }

    @Data
    @AllArgsConstructor
    public static class Event {
        public final long id;
        public final short sportId;
        public long plannedStartTimestamp;
        public String name;
        public final Map<OutcomeKey, OutcomeData> outcomeMap;
        public final LiveEventInfo liveEventInfo;

        public boolean hasOutcome(OutcomeKey outcomeKey) {
            return outcomeMap.containsKey(outcomeKey);
        }

        @Override
        public String toString() {
            return "id=" + id +
                    ", sportId=" + sportId +
                    ", plannedStartTimestamp=" + plannedStartTimestamp +
                    ", name='" + name + '\'' +
                    ", outcomeMap=" + outcomeMap;
        }
    }

    @AllArgsConstructor
    public static class LiveEventInfo {
        public String score;

        @Override
        public String toString() {
            return "score='" + score + '\'';
        }
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class OutcomeKey {
        public final short marketAndBetType;
        public final float marketAndBetTypeParam;
        public final short periodIdentifier;
        public final int playerId1;
        public final int playerId2;

        @Override
        public String toString() {
            return marketAndBetType +
                    "," + marketAndBetTypeParam +
                    "," + periodIdentifier +
                    "," + playerId1 +
                    "," + playerId2;
        }
    }

    @AllArgsConstructor
    public static class OutcomeData {
        float koef;

        @Override
        public String toString() {
            return "koef= " + koef;
        }
    }
}
