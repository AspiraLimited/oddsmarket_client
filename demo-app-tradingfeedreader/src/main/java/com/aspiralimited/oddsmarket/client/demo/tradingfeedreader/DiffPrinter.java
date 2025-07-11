package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.config.statical.Period;
import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.client.TradingFeedReconnectable;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.TradingFeedStateKeepingListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model.InMemoryStateStorage;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.outcomenametranslator.OutcomeNameTranslator;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.outcomenametranslator.ReverseBetSpecCalculator;
import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static java.lang.Thread.sleep;

public class DiffPrinter {
    private final OutcomeNameTranslator outcomeNameTranslator;

    static private final DateFormat matchStartTimeFormat = new SimpleDateFormat("MMM d HH:mm", Locale.ENGLISH);

    public DiffPrinter(DictionariesService dictionariesService) {
        ReverseBetSpecCalculator reverseBetSpecCalculator = new ReverseBetSpecCalculator(
                dictionariesService.getMarketAndBetTypeDtoById(),
                dictionariesService.getBetTypeDtoById()
        );
        outcomeNameTranslator = new OutcomeNameTranslator(
                dictionariesService.getMarketAndBetTypeDtoById(),
                ResourceBundle.getBundle(OutcomeNameTranslator.RESOURCE_BUNDLE_NAME),
                reverseBetSpecCalculator
        );

    }

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
                            printToConsole("Initial state transferring");
                            break;
                        case EVENTSNAPSHOT:
                            OddsmarketTradingDto.EventSnapshot eventSnapshot = serverMessage.getEventSnapshot();
                            long eventId = eventSnapshot.getEventId();
                            printToConsole("[NEW] " + constructEventName(eventId) + " " + inMemoryStateStorage.getEvent(eventId));
                            break;
                        case EVENTPATCH:
                            if (initialStateTransferred) {
                                OddsmarketTradingDto.EventPatch eventPatch = serverMessage.getEventPatch();
                                long eventPatchEventId = eventPatch.getEventId();
                                String eventName = constructEventName(eventPatchEventId);
                                InMemoryStateStorage.Event cachedEvent = inMemoryStateStorage.getEventByEventId().get(eventPatchEventId);
                                short sportId = cachedEvent.sportId;
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
                                    for (OddsmarketTradingDto.OutcomeSnapshot outcomeSnapshot : marketSnapshot.getOutcomesList()) {
                                        OddsmarketTradingDto.OutcomeKey protobufOutcomeKey = outcomeSnapshot.getOutcomeKey();
                                        InMemoryStateStorage.OutcomeKey outcomeKey = new InMemoryStateStorage.OutcomeKey((short) protobufOutcomeKey.getMarketAndBetTypeId(),
                                                protobufOutcomeKey.getMarketAndBetTypeParam(),
                                                (short) protobufOutcomeKey.getPeriodIdentifier(),
                                                protobufOutcomeKey.getPlayerId1(),
                                                protobufOutcomeKey.getPlayerId2()
                                        );
                                        OddsmarketTradingDto.OutcomeData protobufOutcomeData = outcomeSnapshot.getOutcomeData();
                                        InMemoryStateStorage.OutcomeData outcomeData = inMemoryStateStorage.protobufOutcomeDataToOutcomeData(protobufOutcomeData);
                                        String outcomeName = generateOutcomeName(outcomeKey, sportId);
                                        if (cachedEvent.hasOutcome(outcomeKey)) {
                                            if (protobufOutcomeData.getOdds() > 0) {
                                                printToConsole("    [UPD] " + outcomeName + ": " + outcomeData);
                                            } else {
                                                printToConsole("    [DEL] " + outcomeName);
                                            }
                                        } else {
                                            if (protobufOutcomeData.getOdds() > 0) {
                                                printToConsole("    [NEW] " + outcomeName + ": " + outcomeData);
                                            } else {
                                                throw new IllegalStateException("Missing outcome is being deactivated. ID=" + outcomeKey);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case EVENTSREMOVED:
                            OddsmarketTradingDto.EventsRemoved eventsRemoved = serverMessage.getEventsRemoved();
                            for (Long removedEventId : eventsRemoved.getEventIdsList()) {
                                if (inMemoryStateStorage.hasEvent(removedEventId)) {
                                    printToConsole("[DEL] " + constructEventName(removedEventId) + " [#" + removedEventId + "]");
                                } else {
                                    throw new IllegalStateException("Missing bookmaker event information when trying to remove bookmaker event id=" + removedEventId);
                                }
                            }
                            break;
                        case INITIALSYNCCOMPLETE:
                            initialStateTransferred = true;
                            printToConsole("Initial state transferred");
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
                            .mapToInt(value -> value.outcomeMap.size())
                            .sum();
                    printToConsole("[STATS SNAPSHOT] [" + Instant.now() + "] Bookmaker events count: " + inMemoryStateStorage.getEventByEventId().size() + "; Outcomes count: " + outcomesCount
                    );
                }
            }

            @Override
            public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
                super.onConnectError(tradingFeedConnectionStatusCode);
            }

            @Override
            public void onDisconnected(TradingFeedReconnectable tradingFeedReconnectable) {
                super.onDisconnected(tradingFeedReconnectable);
                printToConsole("Connection lost");
                System.exit(0);
            }

            private String constructEventName(long eventId) {
                InMemoryStateStorage.Event event = inMemoryStateStorage.getEventByEventId().get(eventId);
                return DiffPrinter.this.constructEventName(event.getName(), event.getPlannedStartTimestamp());
            }
        };
        TradingFeedClient.authenticateAndSubscribe(feedWebsocketUrl, apiKey, bookmakerId, tradingFeedListener);
        sleep(1_000_000_000);
    }

    private String constructEventName(OddsmarketTradingDto.EventSnapshot eventSnapshot) {
        OddsmarketTradingDto.EventMetadata eventMetadata = eventSnapshot.getEventMetadata();
        if (eventMetadata.getNamesCount() == 0) {
            return "No name event";
        }
        return DiffPrinter.this.constructEventName(eventMetadata.getNames(0).getName(), eventMetadata.getPlannedStartTimestamp());
    }

    private String constructEventName(String name, long startedAt) {
        return name + " [" + longToDateTimeWithMinutePrecisionWitoutYear(startedAt * 1000) + "]";
    }

    static public String longToDateTimeWithMinutePrecisionWitoutYear(long datetime) {
        return matchStartTimeFormat.format(new Date(datetime));
    }

    private String generateOutcomeName(InMemoryStateStorage.OutcomeKey outcomeKey, short sportId) {
        String periodName = Period.periodName(outcomeKey.periodIdentifier, sportId, true);
        return outcomeNameTranslator.translate(
                outcomeKey.marketAndBetType,
                outcomeKey.marketAndBetTypeParam,
                false,
                false
        ).getEffectiveName() + " [" + periodName + "]";
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
