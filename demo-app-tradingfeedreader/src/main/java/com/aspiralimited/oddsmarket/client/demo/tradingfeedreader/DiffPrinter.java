package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedSubscriptionConfig;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.TradingFeedStateKeepingListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model.InMemoryStateStorage;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.aspiralimited.oddsmarket.client.v4.rest.OddsmarketRestHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@RequiredArgsConstructor
public class DiffPrinter {

    private final OddsmarketRestHttpClient oddsmarketRestHttpClient;

    static private final DateFormat matchStartTimeFormat = new SimpleDateFormat("MMM d HH:mm", Locale.ENGLISH);
    private final Map<String, String> periodNameByPeriodIdentifierAndSportId = new HashMap<>();

    @SneakyThrows
    public void listenFeedAndPrintDiffs(String feedWebsocketUrl, String apiKey, short bookmakerId, Set<Integer> sportIds) {

        TradingFeedListener tradingFeedListener = new TradingFeedStateKeepingListener() {
            long lastStatsPrintedAt = System.currentTimeMillis();
            volatile boolean initialStateTransferred = false;

            @Override
            public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
                if (serverMessage.getPayloadCase() != OddsmarketTradingDto.ServerMessage.PayloadCase.EVENTPATCH) {
                    super.onServerMessage(serverMessage);
                }
                try {
                    switch (serverMessage.getPayloadCase()) {
                        case SESSIONSTART:
                            printToConsole(new Date() + ": Initial state transferring");
                            break;
                        case EVENTSNAPSHOT:
                            OddsmarketTradingDto.EventSnapshot eventSnapshot = serverMessage.getEventSnapshot();
                            long eventId = eventSnapshot.getEventId();

                            printToConsole("[NEW] " + constructEventName(eventId) + " " + eventSnapshotToString(eventSnapshot));
                            break;
                        case EVENTPATCH:
                            if (initialStateTransferred) {
                                OddsmarketTradingDto.EventPatch eventPatch = serverMessage.getEventPatch();
                                long eventPatchEventId = eventPatch.getEventId();
                                String eventName = constructEventName(eventPatchEventId);
                                InMemoryStateStorage.Event cachedEvent = inMemoryStateStorage.getEventByEventId().get(eventPatchEventId);
                                //short sportId = cachedEvent.sportId;
                                if (eventPatch.hasUpdatedLiveEventInfo()) {
                                    InMemoryStateStorage.LiveEventInfo liveEventInfo = inMemoryStateStorage.protobufLiveEventInfoToLiveEventInfo(eventPatch.getUpdatedLiveEventInfo());
                                    if (liveEventInfo.score != null) {
                                        printToConsole("[LIVE] " + eventName + ": " + liveEventInfo);
                                    }
                                }
                                if (eventPatch.hasUpdatedEventMetadata()) {
                                    printToConsole("[UPD] " + eventName + ": " + eventPatch.getUpdatedEventMetadata());
                                }

                                printToConsole("[ODDS] " + eventName);
                                for (OddsmarketTradingDto.MarketSnapshot marketSnapshot : eventPatch.getUpdatedMarketsList()) {
                                    OddsmarketTradingDto.MarketKey protobufMarketKey = marketSnapshot.getMarketKey();
                                    InMemoryStateStorage.MarketKey marketKey = new InMemoryStateStorage.MarketKey(
                                            (short) protobufMarketKey.getMarketId(),
                                            protobufMarketKey.getMarketParam(),
                                            (short) protobufMarketKey.getPeriodIdentifier()
                                    );
                                    String market = marketSnapshotToString(marketSnapshot);
                                    if (cachedEvent.hasMarket(marketKey)) {
                                        if (!marketSnapshot.getOutcomesList().isEmpty()) {
                                            printToConsole("    [UPD] " + market);
                                        } else {
                                            printToConsole("    [DEL] " + market);
                                        }
                                    } else {
                                        if (!marketSnapshot.getOutcomesList().isEmpty()) {
                                            printToConsole("    [NEW] " + market);
                                        } else {
                                            throw new IllegalStateException("Missing market is being deactivated. Market: " + market);
                                        }
                                    }
                                }
                            }
                            break;
                        case EVENTSREMOVED:
                            OddsmarketTradingDto.EventsRemoved eventsRemoved = serverMessage.getEventsRemoved();
                            for (Long removedEventId : eventsRemoved.getEventIdsList()) {
                                printToConsole("[DEL] " + constructEventName(removedEventId) + " [#" + removedEventId + "]");
                            }

                            break;
                        case INITIALSYNCCOMPLETE:
                            initialStateTransferred = true;
                            printToConsole(new Date() + "Initial state transferred");
                            break;
                        case HEARTBEAT:
                            break;
                        case ERRORMESSAGE:
                            OddsmarketTradingDto.ErrorMessage errorMessage = serverMessage.getErrorMessage();
                            System.err.println(errorMessage);
                            break;
                        case PAYLOAD_NOT_SET:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (serverMessage.getPayloadCase() == OddsmarketTradingDto.ServerMessage.PayloadCase.EVENTPATCH) {
                    super.onServerMessage(serverMessage);
                }
                if (
                        serverMessage.getPayloadCase() == OddsmarketTradingDto.ServerMessage.PayloadCase.EVENTSNAPSHOT
                                && lastStatsPrintedAt + 60_000 < System.currentTimeMillis()
                ) {
                    lastStatsPrintedAt = System.currentTimeMillis();
                    int outcomesCount = inMemoryStateStorage.getEventByEventId().values().stream()
                            .mapToInt(value -> value.outcomesByMarket.size())
                            .sum();
                    printToConsole("[STATS SNAPSHOT] [" + Instant.now() + "] Bookmaker events count: " + inMemoryStateStorage.getEventByEventId().size() + "; Outcomes count: " + outcomesCount
                    );
                }
            }

            @Override
            public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
                super.onConnectError(tradingFeedConnectionStatusCode);
                System.err.println("Error during connection: " + tradingFeedConnectionStatusCode);
            }

            private String constructEventName(long eventId) {
                InMemoryStateStorage.Event event = inMemoryStateStorage.getEventByEventId().get(eventId);
                if (event == null) {
                    return "No name event";
                }
                return DiffPrinter.this.constructEventName(event.getName(), event.getPlannedStartTimestamp(), event.leagueName);
            }

        };
        TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig = TradingFeedSubscriptionConfig.builder()
                .apiKey(apiKey)
                .tradingFeedId(bookmakerId)
                .build();
        TradingFeedClient.builder()
                .host(feedWebsocketUrl)
                .tradingFeedSubscriptionConfig(tradingFeedSubscriptionConfig)
                .tradingFeedListener(tradingFeedListener)
                .build()
                .connect();
        sleep(1_000_000_000);
    }

    public String eventSnapshotToString(OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        return eventSnapshot.getMarketsList().stream()
                .map(this::marketSnapshotToString)
                .collect(Collectors.joining("; "));
    }

    private String marketSnapshotToString(OddsmarketTradingDto.MarketSnapshot marketSnapshot) {
        String marketTitle = marketSnapshot.getMarketInfo().getMarketTitles(0).getName();
        if (marketSnapshot.getOutcomesList().isEmpty()) {
            return marketTitle;
        }
        return marketTitle + " {" + marketSnapshot.getOutcomesList()
                .stream()
                .map(outcomeSnapshot -> {
                    OddsmarketTradingDto.OutcomeData outcomeData = outcomeSnapshot.getOutcomeData();
                    return outcomeData.getShortOutcomeTitle() + "(" + outcomeData.getOdds() + ")";
                })
                .collect(Collectors.joining(", "))
                + "}";
    }

    private String constructEventName(String name, long startedAt, String leagueName) {
        return name + " [" + longToDateTimeWithMinutePrecisionWitoutYear(startedAt) + "] "+leagueName;
    }

    static public String longToDateTimeWithMinutePrecisionWitoutYear(long datetime) {
        return matchStartTimeFormat.format(new Date(datetime));
    }

    private String generateOutcomeName(short sportId, InMemoryStateStorage.OutcomeKey outcomeKey, InMemoryStateStorage.OutcomeData outcomeData) throws ExecutionException, InterruptedException {
        String periodIdentifierAndSportId = outcomeKey.periodIdentifier + "_" + sportId;
        String periodName = periodNameByPeriodIdentifierAndSportId.computeIfAbsent(periodIdentifierAndSportId, key -> {
            try {
                return oddsmarketRestHttpClient.getPeriodName(outcomeKey.periodIdentifier, sportId).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while getting period name", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Failed to get period name", e);
            }
        });
        return outcomeData.title + " [" + periodName + "]";
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
