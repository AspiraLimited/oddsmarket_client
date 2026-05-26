package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                eventMetadata.hasLeague() ? eventMetadata.getLeague().getId() : 0L,
                eventMetadata.getRawEventId(),
                eventMetadata.getPlannedStartTimestamp(),
                eventMetadata.getUniformEventId(),
                eventMetadata.getEventType(),
                extractEventName(eventMetadata),
                extractLeagueName(eventMetadata),
                protobufMarketsToMarkets(eventSnapshot.getMarketsList()),
                protobufLiveEventInfoToLiveEventInfo(eventSnapshot.getLiveEventInfo()),
                eventMetadata.getDirectLink(),
                protobufEventPlayersToEventPlayerData(eventSnapshot.getPlayersList())
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
        Map<ScoreKey, DetailedScoreData> detailedScores = new LinkedHashMap<>();
        for (OddsmarketTradingDto.DetailedScore detailedScore : protobufLiveEventInfo.getDetailedScoresList()) {
            ScoreKey scoreKey = new ScoreKey(
                    detailedScore.getScoreKey().getScoreType(),
                    detailedScore.getScoreKey().getPeriodIdentifier()
            );
            detailedScores.put(scoreKey, new DetailedScoreData(detailedScore.getHome(), detailedScore.getAway()));
        }
        return new LiveEventInfo(
                protobufLiveEventInfo.hasCurrentPeriodIdentifier() ? protobufLiveEventInfo.getCurrentPeriodIdentifier() : null,
                protobufLiveEventInfo.hasCurrentSecond() ? protobufLiveEventInfo.getCurrentSecond() : null,
                protobufLiveEventInfo.getMatchStatus(),
                detailedScores,
                extractMainScore(protobufLiveEventInfo)
        );
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
            result.put(marketKey, protobufOutcomesToMarkets(marketSnapshot.getOutcomesList()));
        }
        return result;
    }

    private Map<OutcomeKey, OutcomeData> protobufOutcomesToMarkets(List<OddsmarketTradingDto.OutcomeSnapshot> outcomeSnapshots) {
        Map<OutcomeKey, OutcomeData> outcomeDataByOutcomeKey = new HashMap<>();
        for (OddsmarketTradingDto.OutcomeSnapshot outcomeSnapshot : outcomeSnapshots) {
            OutcomeKey outcomeKey = protobufOutcomeKeyToOutcomeKey(outcomeSnapshot.getOutcomeKey());
            OutcomeData outcomeData = protobufOutcomeDataToOutcomeData(outcomeSnapshot.getOutcomeData());
            outcomeDataByOutcomeKey.put(outcomeKey, outcomeData);
        }
        return outcomeDataByOutcomeKey;
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
                protobufOutcomeData.getRawOutcomeId(),
                protobufOutcomeData.getDirectLink()
        );
    }

    private Map<Integer, EventPlayerData> protobufEventPlayersToEventPlayerData(List<OddsmarketTradingDto.EventPlayer> eventPlayers) {
        Map<Integer, EventPlayerData> result = new HashMap<>();
        for (OddsmarketTradingDto.EventPlayer eventPlayer : eventPlayers) {
            if (eventPlayer.getActive()) {
                result.put(eventPlayer.getPlayer().getId(), protobufEventPlayerToEventPlayerData(eventPlayer));

            }
        }
        return result;
    }

    private EventPlayerData protobufEventPlayerToEventPlayerData(OddsmarketTradingDto.EventPlayer eventPlayer) {
        String name = null;
        if (eventPlayer.getPlayer().getNamesCount() > 0) {
            name = eventPlayer.getPlayer().getNames(0).getName();
        }
        return new EventPlayerData(name, eventPlayer.hasHome() ? eventPlayer.getHome() : null);
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
        if (eventPatch.hasUpdatedEventMetadata()) {
            applyUpdatedEventMetadata(event, eventPatch.getUpdatedEventMetadata());
        }
        if (eventPatch.hasUpdatedLiveEventInfo()) {
            event.liveEventInfo = protobufLiveEventInfoToLiveEventInfo(eventPatch.getUpdatedLiveEventInfo());
        }
        if (eventPatch.getUpdatedMarketsCount() > 0) {
            for (OddsmarketTradingDto.MarketSnapshot marketSnapshot : eventPatch.getUpdatedMarketsList()) {
                OddsmarketTradingDto.MarketKey protobufMarketKey = marketSnapshot.getMarketKey();
                MarketKey marketKey = new MarketKey(
                        (short) protobufMarketKey.getMarketId(),
                        protobufMarketKey.getMarketParam(),
                        (short) protobufMarketKey.getPeriodIdentifier()
                );
                if (marketSnapshot.getOutcomesCount() == 0) {
                    event.outcomesByMarket.remove(marketKey);
                } else {
                    event.outcomesByMarket.put(marketKey, protobufOutcomesToMarkets(marketSnapshot.getOutcomesList()));
                }
            }
        }
        for (OddsmarketTradingDto.EventPlayer eventPlayer : eventPatch.getUpdatedPlayersList()) {
            if (eventPlayer.getActive()) {
                event.players.put(eventPlayer.getPlayer().getId(), protobufEventPlayerToEventPlayerData(eventPlayer));
            } else {
                event.players.remove(eventPlayer.getPlayer().getId());
            }
        }
    }

    private void applyUpdatedEventMetadata(Event event, OddsmarketTradingDto.EventMetadata eventMetadata) {
        event.sportId = (short) eventMetadata.getSportId();
        event.homeId = eventMetadata.getHome().getId();
        event.awayId = eventMetadata.getAway().getId();
        event.leagueId = eventMetadata.hasLeague() ? eventMetadata.getLeague().getId() : 0L;
        event.rawEventId = eventMetadata.getRawEventId();
        event.plannedStartTimestamp = eventMetadata.getPlannedStartTimestamp();
        event.uniformEventId = eventMetadata.getUniformEventId();
        event.eventType = eventMetadata.getEventType();
        event.name = extractEventName(eventMetadata);
        event.leagueName = extractLeagueName(eventMetadata);
        event.directLink = eventMetadata.getDirectLink();
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
        public short sportId;
        public long homeId;
        public long awayId;
        public long leagueId;
        public String rawEventId;
        public long plannedStartTimestamp;
        public long uniformEventId;
        public OddsmarketTradingDto.EventType eventType;
        public String name;
        public String leagueName;
        public final Map<MarketKey, Map<OutcomeKey, OutcomeData>> outcomesByMarket;
        public LiveEventInfo liveEventInfo;
        public String directLink;
        public final Map<Integer, EventPlayerData> players;

        public Event copy() {
            Map<MarketKey, Map<OutcomeKey, OutcomeData>> outcomesByMarketCopy = new HashMap<>();
            for (Map.Entry<MarketKey, Map<OutcomeKey, OutcomeData>> outcomesByMarketEntry : outcomesByMarket.entrySet()) {
                Map<OutcomeKey, OutcomeData> outcomesCopy = new HashMap<>();
                for (Map.Entry<OutcomeKey, OutcomeData> outcomeKeyOutcomeDataEntry : outcomesByMarketEntry.getValue().entrySet()) {
                    OutcomeKey outcomeKey = outcomeKeyOutcomeDataEntry.getKey();
                    OutcomeData outcomeData = outcomeKeyOutcomeDataEntry.getValue();
                    outcomesCopy.put(outcomeKey.copy(), outcomeData.copy());
                }
                MarketKey marketKey = outcomesByMarketEntry.getKey();
                outcomesByMarketCopy.put(marketKey.copy(), outcomesCopy);
            }
            Map<Integer, EventPlayerData> playersCopy = new HashMap<>();
            for (Map.Entry<Integer, EventPlayerData> playerEntry : players.entrySet()) {
                playersCopy.put(playerEntry.getKey(), playerEntry.getValue().copy());
            }
            return new Event(
                    id,
                    sportId,
                    homeId,
                    awayId,
                    leagueId,
                    rawEventId,
                    plannedStartTimestamp,
                    uniformEventId,
                    eventType,
                    name,
                    leagueName,
                    outcomesByMarketCopy,
                    liveEventInfo == null ? null : liveEventInfo.copy(),
                    directLink,
                    playersCopy
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
    @Data
    public static class LiveEventInfo {
        public Integer currentPeriodIdentifier;
        public Integer currentSecond;
        public OddsmarketTradingDto.LiveMatchStatus matchStatus;
        public Map<ScoreKey, DetailedScoreData> detailedScores;
        public String score;

        public LiveEventInfo copy() {
            Map<ScoreKey, DetailedScoreData> detailedScoresCopy = new LinkedHashMap<>();
            for (Map.Entry<ScoreKey, DetailedScoreData> scoreEntry : detailedScores.entrySet()) {
                detailedScoresCopy.put(scoreEntry.getKey().copy(), scoreEntry.getValue().copy());
            }
            return new LiveEventInfo(
                    currentPeriodIdentifier,
                    currentSecond,
                    matchStatus,
                    detailedScoresCopy,
                    score
            );
        }
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class ScoreKey {
        public final OddsmarketTradingDto.ScoreType scoreType;
        public final int periodIdentifier;

        public ScoreKey copy() {
            return new ScoreKey(scoreType, periodIdentifier);
        }
    }

    @Data
    @AllArgsConstructor
    public static class DetailedScoreData {
        public int home;
        public int away;

        public DetailedScoreData copy() {
            return new DetailedScoreData(home, away);
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
        public String directLink;

        @Override
        public String toString() {
            return "koef= " + koef;
        }

        public OutcomeData copy() {
            return new OutcomeData(
                    title,
                    koef,
                    rawOutcomeId,
                    directLink
            );
        }
    }

    @AllArgsConstructor
    @Data
    public static class EventPlayerData {
        public String name;
        public Boolean isHome;

        public EventPlayerData copy() {
            return new EventPlayerData(name, isHome);
        }
    }
}
