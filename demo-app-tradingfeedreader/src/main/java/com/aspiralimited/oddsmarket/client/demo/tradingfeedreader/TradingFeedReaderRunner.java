package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording.TradingFeedSessionRecorder;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedClient;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.TradingFeedSubscriptionConfig;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.TradingFeedStateKeepingListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SUMMARY_FILENAME;
import static java.lang.Thread.sleep;

/**
 * Entry-point orchestrator for a trading feed reader session: connects to the websocket,
 * forwards every message to the {@link TradingFeedSessionRecorder} for on-disk recording,
 * tracks aggregate stats, prints a startup banner once the subscription succeeds, and writes
 * both a human-readable text summary and a machine-readable {@code summary.json} on shutdown.
 *
 * <p>Exit codes:
 * <ul>
 *     <li>{@code 0} — graceful stop (Ctrl+C, {@code --duration} elapsed, {@code --maxMessages} reached)</li>
 *     <li>{@code 1} — unexpected error before the shutdown hook was registered</li>
 *     <li>{@code 2} — fatal subscription error (AUTH_FAILED / SUBSCRIPTION_FAILED / BAD_REQUEST)</li>
 *     <li>{@code 4} — writer queue overflow (disk I/O cannot keep up with incoming messages)</li>
 * </ul>
 * The exit code is enforced via {@link Runtime#halt(int)} at the end of the shutdown hook,
 * overriding the JVM default for SIGINT (which would otherwise be 130 on Unix).
 */
public class TradingFeedReaderRunner {

    @SneakyThrows
    public void run(TradingFeedReaderConfiguration configuration) {
        TradingFeedSessionRecorder recorder = new TradingFeedSessionRecorder(configuration);
        SessionExitState exitState = new SessionExitState();

        String feedWebsocketUrl = (configuration.getFeedDomain().startsWith("localhost") ? "ws://" : "wss://")
                + configuration.getFeedDomain();

        TradingFeedSessionListener tradingFeedListener = new TradingFeedSessionListener(
                recorder,
                configuration.getMaxMessages(),
                configuration.getDuration(),
                exitState
        );

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
                tradingFeedListener.finalizeSession();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Runtime.getRuntime().halt(exitState.exitCode());
        }));

        System.out.println("Connecting to " + feedWebsocketUrl + " ...");
        client.connect();

        if (configuration.getDuration() != null) {
            scheduleDurationStop(configuration.getDuration(), exitState);
        }

        sleep(Integer.MAX_VALUE);
    }

    private void scheduleDurationStop(Duration duration, SessionExitState exitState) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "duration-stop");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.schedule(() -> {
            System.out.println();
            System.out.println("Duration limit (" + formatDuration(duration) + ") reached — stopping gracefully.");
            exitState.markDurationLimit();
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

    /**
     * Mutable state shared between the listener (which knows about fatal/max-messages stops)
     * and the duration scheduler. Read by the shutdown hook to decide the final exit code
     * and by {@link TradingFeedSessionListener#finalizeSession()} to populate {@code summary.json}.
     *
     * <p>Default values reflect Ctrl+C: nobody set anything, so we exit with code 0 and the reason "ctrl_c".
     */
    private static class SessionExitState {
        private final AtomicInteger exitCode = new AtomicInteger(0);
        private final AtomicReference<String> reason = new AtomicReference<>("ctrl_c");
        private volatile String fatalErrorCode;

        void markDurationLimit() {
            exitCode.set(0);
            reason.set("duration_limit");
        }

        void markMaxMessagesLimit() {
            exitCode.set(0);
            reason.set("max_messages_limit");
        }

        void markFatal(TradingFeedConnectionStatusCode code) {
            exitCode.set(2);
            reason.set("fatal_error");
            fatalErrorCode = code.name();
        }

        void markWriteQueueOverflow() {
            exitCode.set(4);
            reason.set("writer_queue_overflow");
        }

        int exitCode() {
            return exitCode.get();
        }

        String reason() {
            return reason.get();
        }

        String fatalErrorCode() {
            return fatalErrorCode;
        }
    }

    private static class TradingFeedSessionListener extends TradingFeedStateKeepingListener {
        private final TradingFeedSessionRecorder recorder;
        private final Long maxMessages;
        private final Duration durationLimit;
        private final SessionExitState exitState;
        private final ObjectMapper jsonMapper;
        private final Instant sessionStartedAt = Instant.now();
        private final Map<String, Long> seenMessageTypeCounters = new TreeMap<>();
        private final Set<Long> seenEventIds = new HashSet<>();
        private long messagesSeenTotal;
        private String sessionId;
        private boolean initialSyncSeen;

        private TradingFeedSessionListener(
                TradingFeedSessionRecorder recorder,
                Long maxMessages,
                Duration durationLimit,
                SessionExitState exitState
        ) {
            this.recorder = recorder;
            this.maxMessages = maxMessages;
            this.durationLimit = durationLimit;
            this.exitState = exitState;
            this.jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
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
                if (recorder.isWriteQueueOverflowed()) {
                    System.err.println();
                    System.err.println("Writer queue overflowed (capacity "
                            + TradingFeedSessionRecorder.WRITE_QUEUE_CAPACITY
                            + ") — disk I/O cannot keep up with incoming messages. Stopping to avoid silent data loss.");
                    exitState.markWriteQueueOverflow();
                    System.exit(4);
                }
                if (maxMessages != null && recorder.getMessagesAcceptedTotal() >= maxMessages) {
                    System.out.println();
                    System.out.println("Max messages limit (" + maxMessages + ") reached — stopping gracefully.");
                    exitState.markMaxMessagesLimit();
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
                exitState.markFatal(tradingFeedConnectionStatusCode);
                System.exit(2);
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

        void finalizeSession() {
            printFinalSummary();
            try {
                writeSummaryJson();
            } catch (Exception e) {
                System.err.println("Failed to write " + SUMMARY_FILENAME);
                e.printStackTrace();
            }
        }

        private void printFinalSummary() {
            Instant endedAt = Instant.now();
            Duration duration = Duration.between(sessionStartedAt, endedAt);

            StringBuilder sb = new StringBuilder();
            sb.append("\n========== Session summary ==========\n");
            sb.append("Started at:              ").append(sessionStartedAt).append("\n");
            sb.append("Ended at:                ").append(endedAt).append("\n");
            sb.append("Duration:                ").append(TradingFeedReaderRunner.formatDuration(duration)).append("\n");
            sb.append("Exit reason:             ").append(exitState.reason());
            if (exitState.fatalErrorCode() != null) {
                sb.append(" (").append(exitState.fatalErrorCode()).append(")");
            }
            sb.append("\n");
            sb.append("Session ID:              ").append(sessionId != null ? sessionId : "(not received)").append("\n");
            sb.append("Initial sync completed:  ").append(initialSyncSeen).append("\n");
            sb.append("Messages seen total:     ").append(messagesSeenTotal).append("\n");
            for (Map.Entry<String, Long> entry : seenMessageTypeCounters.entrySet()) {
                sb.append("  ").append(padRight(entry.getKey() + ":", 22)).append(entry.getValue()).append("\n");
            }
            sb.append("Active events at end:    ").append(inMemoryStateStorage.getEventByEventId().size()).append("\n");
            sb.append("Distinct events seen:    ").append(seenEventIds.size()).append("\n");
            sb.append("\n");

            long recorded = recorder.getMessagesWrittenTotal();
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

        private void writeSummaryJson() throws IOException {
            Instant endedAt = Instant.now();
            Duration sessionDuration = Duration.between(sessionStartedAt, endedAt);
            long recorded = recorder.getMessagesWrittenTotal();
            long accepted = recorder.getMessagesAcceptedTotal();

            ObjectNode root = jsonMapper.createObjectNode();
            root.put("schemaVersion", 1);
            root.put("startedAt", sessionStartedAt.toString());
            root.put("endedAt", endedAt.toString());
            root.put("durationSeconds", sessionDuration.getSeconds());

            ObjectNode exit = root.putObject("exit");
            exit.put("code", exitState.exitCode());
            exit.put("reason", exitState.reason());
            if (exitState.fatalErrorCode() != null) {
                exit.put("fatalErrorCode", exitState.fatalErrorCode());
            } else {
                exit.putNull("fatalErrorCode");
            }

            if (sessionId != null) {
                root.put("sessionId", sessionId);
            } else {
                root.putNull("sessionId");
            }
            root.put("initialSyncCompleted", initialSyncSeen);
            root.put("sessionFolder", recorder.getSessionFolder().toAbsolutePath().toString());

            ObjectNode messagesSeen = root.putObject("messagesSeen");
            messagesSeen.put("total", messagesSeenTotal);
            messagesSeen.set("byType", jsonMapper.valueToTree(seenMessageTypeCounters));

            ObjectNode messagesRecorded = root.putObject("messagesRecorded");
            messagesRecorded.put("total", recorded);
            messagesRecorded.put("accepted", accepted);
            if (messagesSeenTotal == 0) {
                messagesRecorded.putNull("percentOfSeen");
            } else {
                double percent = (recorded * 100.0) / messagesSeenTotal;
                // Round to 1 decimal for stable, agent-friendly numbers.
                messagesRecorded.put("percentOfSeen", Math.round(percent * 10.0) / 10.0);
            }
            messagesRecorded.set("byType", jsonMapper.valueToTree(recorder.getMessageTypeCountersSnapshot()));

            root.put("activeEventsAtEnd", inMemoryStateStorage.getEventByEventId().size());
            root.put("distinctEventsSeen", seenEventIds.size());

            ObjectNode filter = root.putObject("filter");
            filter.put("active", recorder.isFilterActive());
            filter.set("recordOnlyEventIds", longArray(recorder.getRecordOnlyEventIds()));
            filter.set("recordOnlyRawEventIds", stringArray(recorder.getRecordOnlyRawEventIds()));

            ObjectNode limits = root.putObject("limits");
            if (durationLimit != null) {
                limits.put("durationSeconds", durationLimit.getSeconds());
            } else {
                limits.putNull("durationSeconds");
            }
            if (maxMessages != null) {
                limits.put("maxMessages", maxMessages);
            } else {
                limits.putNull("maxMessages");
            }

            Path summaryFile = recorder.getSessionFolder().resolve(SUMMARY_FILENAME);
            Files.writeString(summaryFile, jsonMapper.writeValueAsString(root), StandardCharsets.UTF_8);
        }

        private ArrayNode longArray(Collection<Long> values) {
            ArrayNode array = jsonMapper.createArrayNode();
            if (values == null) {
                return array;
            }
            List<Long> sorted = new ArrayList<>(values);
            sorted.sort(Long::compareTo);
            sorted.forEach(array::add);
            return array;
        }

        private ArrayNode stringArray(Collection<String> values) {
            ArrayNode array = jsonMapper.createArrayNode();
            if (values == null) {
                return array;
            }
            List<String> sorted = new ArrayList<>(values);
            sorted.sort(String::compareTo);
            sorted.forEach(array::add);
            return array;
        }

        private String padRight(String value, int width) {
            if (value.length() >= width) {
                return value;
            }
            return value + " ".repeat(width - value.length());
        }
    }
}
