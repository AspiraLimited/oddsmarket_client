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
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

        ConsolePrintingTradingFeedListener tradingFeedListener = new ConsolePrintingTradingFeedListener(recorder, configuration);

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
            try {
                client.disconnect(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (recorder != null) {
                try {
                    recorder.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                tradingFeedListener.printFinalSummary();
            } catch (Exception e) {
                e.printStackTrace();
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
        private final TradingFeedReaderConfiguration configuration;
        private final Instant sessionStartedAt = Instant.now();
        private final Map<String, Long> seenMessageTypeCounters = new TreeMap<>();
        private final Set<Long> seenEventIds = new HashSet<>();
        private long messagesSeenTotal;
        private String sessionId;
        private boolean initialSyncSeen;
        private long lastStatsPrintedAt = System.currentTimeMillis();

        private ConsolePrintingTradingFeedListener(TradingFeedSessionRecorder recorder, TradingFeedReaderConfiguration configuration) {
            this.recorder = recorder;
            this.configuration = configuration;
        }

        @Override
        public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
            Instant arrivalTimestamp = Instant.now();
            try {
                trackSummaryState(serverMessage);

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

        private void trackSummaryState(OddsmarketTradingDto.ServerMessage serverMessage) {
            messagesSeenTotal++;
            String type = TradingFeedSessionRecorder.messageType(serverMessage);
            seenMessageTypeCounters.merge(type, 1L, Long::sum);

            if (serverMessage.hasSessionStart()) {
                sessionId = serverMessage.getSessionStart().getSessionId();
            } else if (serverMessage.hasInitialSyncComplete()) {
                initialSyncSeen = true;
            } else if (serverMessage.hasEventSnapshot()) {
                seenEventIds.add(serverMessage.getEventSnapshot().getEventId());
            } else if (serverMessage.hasEventPatch()) {
                seenEventIds.add(serverMessage.getEventPatch().getEventId());
            } else if (serverMessage.hasEventsRemoved()) {
                seenEventIds.addAll(serverMessage.getEventsRemoved().getEventIdsList());
            }
        }

        void printFinalSummary() {
            Instant endedAt = Instant.now();
            Duration duration = Duration.between(sessionStartedAt, endedAt);

            StringBuilder sb = new StringBuilder();
            sb.append("\n========== Session summary ==========\n");
            sb.append("Started at:              ").append(sessionStartedAt).append("\n");
            sb.append("Ended at:                ").append(endedAt).append("\n");
            sb.append("Duration:                ").append(formatDuration(duration)).append("\n");
            sb.append("Session ID:              ").append(sessionId != null ? sessionId : "(not received)").append("\n");
            sb.append("Initial sync completed:  ").append(initialSyncSeen).append("\n");
            sb.append("Messages seen total:     ").append(messagesSeenTotal).append("\n");
            for (Map.Entry<String, Long> entry : seenMessageTypeCounters.entrySet()) {
                sb.append("  ").append(padRight(entry.getKey() + ":", 22)).append(entry.getValue()).append("\n");
            }
            sb.append("Active events at end:    ").append(inMemoryStateStorage.getEventByEventId().size()).append("\n");
            sb.append("Distinct events seen:    ").append(seenEventIds.size()).append("\n");
            sb.append("\n");

            if (recorder != null) {
                long recorded = recorder.getMessagesRecordedTotal();
                String percent = messagesSeenTotal == 0
                        ? "n/a"
                        : String.format(Locale.ROOT, "%.1f%%", (recorded * 100.0) / messagesSeenTotal);
                sb.append("Recording: enabled\n");
                sb.append("  Session folder:        ").append(recorder.getSessionFolder().toAbsolutePath()).append("\n");
                sb.append("  Messages recorded:     ").append(recorded).append(" (").append(percent).append(" of seen)\n");
                Map<String, Long> recordedByType = recorder.getMessageTypeCountersSnapshot();
                for (Map.Entry<String, Long> entry : recordedByType.entrySet()) {
                    sb.append("    ").append(padRight(entry.getKey() + ":", 22)).append(entry.getValue()).append("\n");
                }
                if (recorder.isFilterActive()) {
                    sb.append("  Filter:\n");
                    sb.append("    recordOnlyEventIds:    ")
                            .append(recorder.getRecordOnlyEventIds() == null ? "[]" : recorder.getRecordOnlyEventIds())
                            .append("\n");
                    sb.append("    recordOnlyRawEventIds: ")
                            .append(recorder.getRecordOnlyRawEventIds() == null ? "[]" : recorder.getRecordOnlyRawEventIds())
                            .append("\n");
                } else {
                    sb.append("  Filter:                not applied (all messages within subscription scope were recorded)\n");
                }
            } else {
                sb.append("Recording: disabled (--saveMessagesToFolder was not set)\n");
            }

            sb.append("=====================================\n");
            System.out.println(sb);
        }

        private String formatDuration(Duration duration) { // TODO anse - refactor - move to utils this and same code
            long seconds = Math.max(0, duration.getSeconds());
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            if (hours > 0) {
                return String.format(Locale.ROOT, "%dh %dm %ds", hours, minutes, secs);
            }
            if (minutes > 0) {
                return String.format(Locale.ROOT, "%dm %ds", minutes, secs);
            }
            return secs + "s";
        }

        private String padRight(String value, int width) {
            if (value.length() >= width) {
                return value;
            }
            return value + " ".repeat(width - value.length());
        }

        @Override
        public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
            super.onConnectError(tradingFeedConnectionStatusCode);
            System.err.println("Error during connection: " + tradingFeedConnectionStatusCode);
        }
    }
}
