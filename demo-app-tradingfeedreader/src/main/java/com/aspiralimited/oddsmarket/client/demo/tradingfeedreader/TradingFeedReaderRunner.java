package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording.TradingFeedSessionRecorder;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedSubscriptionConfig;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.TradingFeedStateKeepingListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import lombok.SneakyThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Entry-point orchestrator for a trading feed reader session: connects to the websocket,
 * forwards every message to the {@link TradingFeedSessionRecorder} for on-disk recording,
 * tracks aggregate stats, prints a startup banner once the subscription succeeds, and prints
 * a final summary on shutdown.
 */
public class TradingFeedReaderRunner {

    @SneakyThrows
    public void run(TradingFeedReaderConfiguration configuration) {
        TradingFeedSessionRecorder recorder = new TradingFeedSessionRecorder(configuration);

        String feedWebsocketUrl = (configuration.getFeedDomain().startsWith("localhost") ? "ws://" : "wss://")
                + configuration.getFeedDomain();

        TradingFeedSessionListener tradingFeedListener = new TradingFeedSessionListener(recorder, configuration.getMaxMessages());

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
            try {
                recorder.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                tradingFeedListener.printFinalSummary();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        System.out.println("Connecting to " + feedWebsocketUrl + " ...");
        client.connect();

        if (configuration.getDuration() != null) {
            scheduleDurationStop(configuration.getDuration());
        }

        sleep(1_000_000_000);
    }

    private void scheduleDurationStop(Duration duration) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "duration-stop");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.schedule(() -> {
            System.out.println();
            System.out.println("Duration limit (" + formatDuration(duration) + ") reached — stopping gracefully.");
            System.exit(0);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static String formatDuration(Duration duration) {
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


    private static class TradingFeedSessionListener extends TradingFeedStateKeepingListener {
        private final TradingFeedSessionRecorder recorder;
        private final Long maxMessages;
        private final Instant sessionStartedAt = Instant.now();
        private final Map<String, Long> seenMessageTypeCounters = new TreeMap<>();
        private final Set<Long> seenEventIds = new HashSet<>();
        private long messagesSeenTotal;
        private String sessionId;
        private boolean initialSyncSeen;

        private TradingFeedSessionListener(TradingFeedSessionRecorder recorder, Long maxMessages) {
            this.recorder = recorder;
            this.maxMessages = maxMessages;
        }

        @Override
        public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
            Instant arrivalTimestamp = Instant.now();
            try {
                boolean firstSessionStart = serverMessage.hasSessionStart() && sessionId == null;
                trackSummaryState(serverMessage);
                if (firstSessionStart) {
                    printSubscribedBanner();
                }
                if (serverMessage.hasErrorMessage()) {
                    System.err.println(serverMessage.getErrorMessage());
                }
                super.onServerMessage(serverMessage);
                recorder.recordMessage(serverMessage, arrivalTimestamp, inMemoryStateStorage);
                if (maxMessages != null && recorder.getMessagesRecordedTotal() >= maxMessages) {
                    System.out.println();
                    System.out.println("Max messages limit (" + maxMessages + ") reached — stopping gracefully.");
                    System.exit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void printSubscribedBanner() {
            String folder = recorder.getSessionFolder().toAbsolutePath().toString();
            System.out.println();
            System.out.println("---------------------------------------------------------------");
            System.out.println(" Successfully subscribed!");
            System.out.println(" Messages are being saved to:");
            System.out.println("   " + folder);
            System.out.println(" Open this folder anytime to inspect the incoming messages.");
            System.out.println();
            System.out.println(" Press Ctrl+C to stop consuming messages.");
            System.out.println("---------------------------------------------------------------");
            System.out.println();
        }

        @Override
        public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
            super.onConnectError(tradingFeedConnectionStatusCode);
            System.err.println("Error during connection: " + tradingFeedConnectionStatusCode);
            if (isFatal(tradingFeedConnectionStatusCode)) {
                System.err.println("This error is fatal — stopping. Check your API key, Trading Feed ID, and that your account has access to the requested feed.");
                System.exit(1);
            }
        }

        private static boolean isFatal(TradingFeedConnectionStatusCode code) {
            return code == TradingFeedConnectionStatusCode.BAD_REQUEST
                    || code == TradingFeedConnectionStatusCode.AUTHENTICATION_FAILED
                    || code == TradingFeedConnectionStatusCode.SUBSCRIPTION_FAILED;
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
            sb.append("Duration:                ").append(TradingFeedReaderRunner.formatDuration(duration)).append("\n");
            sb.append("Session ID:              ").append(sessionId != null ? sessionId : "(not received)").append("\n");
            sb.append("Initial sync completed:  ").append(initialSyncSeen).append("\n");
            sb.append("Messages seen total:     ").append(messagesSeenTotal).append("\n");
            for (Map.Entry<String, Long> entry : seenMessageTypeCounters.entrySet()) {
                sb.append("  ").append(padRight(entry.getKey() + ":", 22)).append(entry.getValue()).append("\n");
            }
            sb.append("Active events at end:    ").append(inMemoryStateStorage.getEventByEventId().size()).append("\n");
            sb.append("Distinct events seen:    ").append(seenEventIds.size()).append("\n");
            sb.append("\n");

            long recorded = recorder.getMessagesRecordedTotal();
            String percent = messagesSeenTotal == 0
                    ? "n/a"
                    : String.format(Locale.ROOT, "%.1f%%", (recorded * 100.0) / messagesSeenTotal);
            sb.append("Recording:\n");
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

            sb.append("=====================================\n");
            System.out.println(sb);
        }

        private String padRight(String value, int width) {
            if (value.length() >= width) {
                return value;
            }
            return value + " ".repeat(width - value.length());
        }
    }
}
