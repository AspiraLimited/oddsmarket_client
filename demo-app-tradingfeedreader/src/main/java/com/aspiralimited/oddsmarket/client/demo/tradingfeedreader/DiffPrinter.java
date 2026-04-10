package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording.TradingFeedSessionRecorder;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedSubscriptionConfig;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.TradingFeedStateKeepingListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model.InMemoryStateStorage;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.aspiralimited.oddsmarket.client.v4.rest.OddsmarketRestHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@RequiredArgsConstructor
public class DiffPrinter {

    private final OddsmarketRestHttpClient oddsmarketRestHttpClient;

    static private final DateFormat matchStartTimeFormat = new SimpleDateFormat("MMM d HH:mm", Locale.ENGLISH);

    @SneakyThrows
    public void listenFeedAndPrintDiffs(TradingFeedReaderConfiguration configuration) {
        TradingFeedSessionRecorder recorder = configuration.getSaveMessagesToFolder() != null
                ? new TradingFeedSessionRecorder(configuration)
                : null;

        String feedWebsocketUrl = (configuration.getFeedDomain().startsWith("localhost") ? "ws://" : "wss://")
                + configuration.getFeedDomain();

        ConsolePrintingTradingFeedListener tradingFeedListener = new ConsolePrintingTradingFeedListener(recorder);

        TradingFeedSubscriptionConfig tradingFeedSubscriptionConfig = TradingFeedSubscriptionConfig.builder()
                .apiKey(configuration.getApiKey())
                .tradingFeedId(configuration.getTradingFeedId())
                .sportIds(configuration.getSportIds())
                .locales(configuration.getLocales())
                .rawIdOriginBookmakerId(configuration.getRawIdOriginBookmakerId())
                .fillRawOutcomeId(configuration.getFillRawOutcomeId())
                .fillDirectLink(configuration.getFillDirectLink())
                .build();

        TradingFeedClient client = TradingFeedClient.builder()
                .host(feedWebsocketUrl)
                .tradingFeedSubscriptionConfig(tradingFeedSubscriptionConfig)
                .tradingFeedListener(tradingFeedListener)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.disconnect(true);
            if (recorder != null) {
                try {
                    recorder.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        client.connect();
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
                    if (outcomeSnapshot.getOutcomeData().hasMarketDepth()) {
                        return outcomeData.getShortOutcomeTitle() + "(" + outcomeData.getOdds() + ",depth=" + outcomeData.getMarketDepth() + ")";
                    } else {
                        return outcomeData.getShortOutcomeTitle() + "(" + outcomeData.getOdds() + ")";
                    }
                })
                .collect(Collectors.joining(", "))
                + "}";
    }

    private String constructEventName(InMemoryStateStorage.Event event) {
        if (event == null) {
            return "No name event";
        }
        return constructEventName(event.getName(), event.getPlannedStartTimestamp(), event.getLeagueName());
    }

    private String constructEventName(String name, long startedAt, String leagueName) {
        return name + " [" + longToDateTimeWithMinutePrecisionWitoutYear(startedAt) + "] " + leagueName;
    }

    static public String longToDateTimeWithMinutePrecisionWitoutYear(long datetime) {
        return matchStartTimeFormat.format(new Date(datetime));
    }


    private static void printToConsole(String msg) {
        System.out.println(msg);
    }

    private static void printErrorToConsole(String msg) {
        System.err.println(msg);
    }

    private class ConsolePrintingTradingFeedListener extends TradingFeedStateKeepingListener {
        private final TradingFeedSessionRecorder recorder;
        private long lastStatsPrintedAt = System.currentTimeMillis();

        private ConsolePrintingTradingFeedListener(TradingFeedSessionRecorder recorder) {
            this.recorder = recorder;
        }

        @Override
        public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
            Instant arrivalTimestamp = Instant.now();
            try {
                switch (serverMessage.getPayloadCase()) {
                    case SESSIONSTART:
                        super.onServerMessage(serverMessage);
                        printToConsole(new Date() + ": Initial state transferring");
                        break;
                    case EVENTSNAPSHOT:
                        super.onServerMessage(serverMessage);
                        OddsmarketTradingDto.EventSnapshot eventSnapshot = serverMessage.getEventSnapshot();
                        InMemoryStateStorage.Event snapshotEvent = inMemoryStateStorage.getEventByEventId().get(eventSnapshot.getEventId());
                        printToConsole("[NEW] " + constructEventName(snapshotEvent) + " " + eventSnapshotToString(eventSnapshot));
                        break;
                    case EVENTPATCH:
                        handleEventPatch(serverMessage);
                        break;
                    case EVENTSREMOVED:
                        handleEventsRemoved(serverMessage.getEventsRemoved(), serverMessage);
                        break;
                    case INITIALSYNCCOMPLETE:
                        super.onServerMessage(serverMessage);
                        printToConsole(new Date() + "Initial state transferred");
                        break;
                    case HEARTBEAT:
                        super.onServerMessage(serverMessage);
                        break;
                    case ERRORMESSAGE:
                        super.onServerMessage(serverMessage);
                        System.err.println(serverMessage.getErrorMessage());
                        break;
                    case PAYLOAD_NOT_SET:
                        super.onServerMessage(serverMessage);
                        break;
                }

                if (recorder != null) {
                    recorder.recordMessage(serverMessage, arrivalTimestamp, inMemoryStateStorage);
                }

                if (
                        serverMessage.getPayloadCase() == OddsmarketTradingDto.ServerMessage.PayloadCase.EVENTSNAPSHOT
                                && lastStatsPrintedAt + 60_000 < System.currentTimeMillis()
                ) {
                    lastStatsPrintedAt = System.currentTimeMillis();
                    int outcomesCount = inMemoryStateStorage.getEventByEventId().values().stream()
                            .mapToInt(value -> value.getOutcomesByMarket().size())
                            .sum();
                    printToConsole("[STATS SNAPSHOT] [" + Instant.now() + "] Bookmaker events count: " + inMemoryStateStorage.getEventByEventId().size() + "; Outcomes count: " + outcomesCount
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleEventPatch(OddsmarketTradingDto.ServerMessage serverMessage) {
            OddsmarketTradingDto.EventPatch eventPatch = serverMessage.getEventPatch();
            long eventId = eventPatch.getEventId();
            InMemoryStateStorage.Event cachedEvent = inMemoryStateStorage.getEventByEventId().get(eventId);
            InMemoryStateStorage.Event beforeUpdateEvent = cachedEvent == null ? null : cachedEvent.copy();
            String eventName = constructEventName(cachedEvent);

            if (eventPatch.hasUpdatedLiveEventInfo()) {
                InMemoryStateStorage.LiveEventInfo liveEventInfo = inMemoryStateStorage.protobufLiveEventInfoToLiveEventInfo(eventPatch.getUpdatedLiveEventInfo());
                if (liveEventInfo.getScore() != null) {
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
                if (cachedEvent != null && cachedEvent.hasMarket(marketKey)) {
                    if (!marketSnapshot.getOutcomesList().isEmpty()) {
                        printToConsole("    [UPD] " + market);
                    } else {
                        printToConsole("    [DEL] " + market);
                    }
                } else {
                    if (!marketSnapshot.getOutcomesList().isEmpty()) {
                        printToConsole("    [NEW] " + market);
                    } else {
                        printToConsole("    [DEL] " + market);
                    }
                }
            }

            super.onServerMessage(serverMessage);

            InMemoryStateStorage.Event afterUpdateEvent = inMemoryStateStorage.getEventByEventId().get(eventId);
            if (beforeUpdateEvent != null && beforeUpdateEvent.equals(afterUpdateEvent)) {
                printErrorToConsole("Event unchanged after patch");
            }
        }

        private void handleEventsRemoved(
                OddsmarketTradingDto.EventsRemoved eventsRemoved,
                OddsmarketTradingDto.ServerMessage serverMessage
        ) {
            Map<Long, String> removedEventNames = new LinkedHashMap<>();
            for (Long removedEventId : eventsRemoved.getEventIdsList()) {
                removedEventNames.put(removedEventId, constructEventName(inMemoryStateStorage.getEventByEventId().get(removedEventId)));
            }
            super.onServerMessage(serverMessage);
            for (Map.Entry<Long, String> removedEvent : removedEventNames.entrySet()) {
                printToConsole("[DEL] " + removedEvent.getValue() + " [#" + removedEvent.getKey() + "]");
            }
        }

        @Override
        public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
            super.onConnectError(tradingFeedConnectionStatusCode);
            System.err.println("Error during connection: " + tradingFeedConnectionStatusCode);
        }
    }
}
