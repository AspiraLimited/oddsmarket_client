package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class InMemoryStateStorage {
    private final Map<Long, Event> eventByEventId = new ConcurrentHashMap<>();

    public void addEvent(OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        eventByEventId.put(eventSnapshot.getEventId(), protobufEventToEvent(eventSnapshot));
    }

    private Event protobufEventToEvent(OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        OddsmarketTradingDto.EventMetadata eventMetadata = eventSnapshot.getEventMetadata();
        return new Event(
                eventSnapshot.getEventId(),
                (short) eventMetadata.getSportId(),
                eventMetadata.getHome().getId(),
                eventMetadata.getAway().getId(),
                eventMetadata.getLeague().getId(),
                eventMetadata.getRawEventId(),
                eventMetadata.getPlannedStartTimestamp(),
                extractEventName(eventMetadata),
                extractLeagueName(eventMetadata),
                protobufMarketsToMarkets(eventSnapshot.getMarketsList()),
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

    private String extractLeagueName(OddsmarketTradingDto.EventMetadata eventMetadata) {
        String result = null;
        if (eventMetadata.hasLeague()) {
            OddsmarketTradingDto.League league = eventMetadata.getLeague();
            if (league.getNamesCount() > 0) {
                result = league.getNames(0).getName();
            } else {
                result = "NO LEAGUE NAME";
            }
        } else {
            result = "NO LEAGUE";
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

    private Map<MarketKey, Map<OutcomeKey, OutcomeData>> protobufMarketsToMarkets(List<OddsmarketTradingDto.MarketSnapshot> marketSnapshots) {
        Map<MarketKey, Map<OutcomeKey, OutcomeData>> result = new HashMap<>();
        for (OddsmarketTradingDto.MarketSnapshot marketSnapshot : marketSnapshots) {
            OddsmarketTradingDto.MarketKey protobufMarketKey = marketSnapshot.getMarketKey();
            MarketKey marketKey = new MarketKey(
                    (short) protobufMarketKey.getMarketId(),
                    protobufMarketKey.getMarketParam(),
                    (short) protobufMarketKey.getPeriodIdentifier()
            );
            Map<OutcomeKey, OutcomeData> outcomeDataByOutcomeKey = result.computeIfAbsent(marketKey, k -> new HashMap<>());
            for (OddsmarketTradingDto.OutcomeSnapshot outcomeSnapshot : marketSnapshot.getOutcomesList()) {
                OutcomeKey outcomeKey = protobufOutcomeKeyToOutcomeKey(outcomeSnapshot.getOutcomeKey());
                OutcomeData outcomeData = protobufOutcomeDataToOutcomeData(outcomeSnapshot.getOutcomeData());
                outcomeDataByOutcomeKey.put(outcomeKey, outcomeData);
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
        String title = null;
        if (protobufOutcomeData.getOutcomeTitlesCount() > 0) {
            title = protobufOutcomeData.getOutcomeTitles(0).getName();
        }
        return new OutcomeData(
                title,
                protobufOutcomeData.getOdds(),
                protobufOutcomeData.getRawOutcomeId()
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
            Map<MarketKey, Map<OutcomeKey, OutcomeData>> updatedMarkets = protobufMarketsToMarkets(eventPatch.getUpdatedMarketsList());
            currentEvent.outcomesByMarket.putAll(updatedMarkets);
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
        public final long homeId;
        public final long awayId;
        public final long leagueId;
        public final String rawEventId;
        public long plannedStartTimestamp;
        public String name;
        public String leagueName;
        public final Map<MarketKey, Map<OutcomeKey, OutcomeData>> outcomesByMarket;
        public final LiveEventInfo liveEventInfo;

        public Event copy() {
            Map<MarketKey, Map<OutcomeKey, OutcomeData>> outcomesByMarketCopy = new HashMap<>();
            for (Map.Entry<MarketKey, Map<OutcomeKey, OutcomeData>> outcomesByMarketEntry : outcomesByMarketCopy.entrySet()) {
                Map<OutcomeKey, OutcomeData> outcomesCopy = new HashMap<>();
                for (Map.Entry<OutcomeKey, OutcomeData> outcomeKeyOutcomeDataEntry : outcomesByMarketEntry.getValue().entrySet()) {
                    OutcomeKey outcomeKey = outcomeKeyOutcomeDataEntry.getKey();
                    OutcomeData outcomeData = outcomeKeyOutcomeDataEntry.getValue();
                    outcomesCopy.put(outcomeKey.copy(), outcomeData.copy());
                }
                MarketKey marketKey = outcomesByMarketEntry.getKey();
                outcomesByMarketCopy.put(marketKey.copy(), outcomesCopy);
            }
            return new Event(
                    id,
                    sportId,
                    homeId,
                    awayId,
                    leagueId,
                    rawEventId,
                    plannedStartTimestamp,
                    name,
                    leagueName,
                    outcomesByMarketCopy,
                    liveEventInfo.copy()
                    );
        }

        public boolean hasMarket(MarketKey marketKey) {
            return outcomesByMarket.containsKey(marketKey);
        }

        @Override
        public String toString() {
            return "id=" + id +
                    ", sportId=" + sportId +
                    ", plannedStartTimestamp=" + plannedStartTimestamp +
                    ", name='" + name + '\'' +
                    ", outcomeMap=" + outcomesByMarket;
        }


    }

    @AllArgsConstructor
    public static class LiveEventInfo {
        public String score;

        @Override
        public String toString() {
            return "score='" + score + '\'';
        }

        public LiveEventInfo copy() {
            return new LiveEventInfo(
                    score
            );
        }
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class MarketKey {
        public final short marketId;
        public final float marketParam;
        public final short periodIdentifier;

        public MarketKey copy() {
            return new MarketKey(
                    marketId,
                    marketParam,
                    periodIdentifier
            );
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

        public OutcomeKey copy() {
            return new OutcomeKey(
                    marketAndBetType,
                    marketAndBetTypeParam,
                    periodIdentifier,
                    playerId1,
                    playerId2
            );
        }
    }

    @AllArgsConstructor
    public static class OutcomeData {
        public String title;
        public float koef;
        public String rawOutcomeId;

        @Override
        public String toString() {
            return "koef= " + koef;
        }

        public OutcomeData copy() {
            return new OutcomeData(
                    title,
                    koef,
                    rawOutcomeId
            );
        }
    }
}
