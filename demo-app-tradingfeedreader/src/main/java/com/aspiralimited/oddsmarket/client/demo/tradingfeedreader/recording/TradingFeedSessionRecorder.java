package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model.InMemoryStateStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import com.google.protobuf.util.JsonFormat;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.ARRIVAL_TIMESTAMP_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.CONTENT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.ERROR_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.EVENTS_REMOVED_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.EVENT_PATCH_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.EVENT_SNAPSHOT_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.HEARTBEAT_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INITIAL_SYNC_COMPLETE_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGES_FOLDER_NAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGES_INDEX_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.PAYLOAD_NOT_SET_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_FOLDER_NAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_START_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SUBSCRIPTION_INFO_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SUBSCRIPTION_STATS_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.TEMP_FILE_SUFFIX;

public class TradingFeedSessionRecorder implements Closeable {
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;
    public static final int WRITE_QUEUE_CAPACITY = 50_000;
    private static final long WRITER_SHUTDOWN_TIMEOUT_MS = 10_000L;

    /**
     * Sentinel value placed on the queue by {@link #close()} to signal the writer thread
     * to drain remaining entries and exit.
     */
    private static final PendingWrite POISON_PILL = new PendingWrite(null, null, null, null, 0L, null);

    private final TradingFeedReaderConfiguration configuration;
    @Getter
    private final Path sessionFolder;
    private final Path messagesFolder;
    private final Path subscriptionStatsFile;
    private final Path messagesIndexFile;
    private final ObjectMapper objectMapper;
    private final ObjectWriter compactJsonWriter;
    private final JsonFormat.Printer protobufPrinter;
    private final Map<String, Long> messageTypeCounters = new TreeMap<>();
    private final Map<Long, String> seenEventNames = new TreeMap<>();
    private final Map<Long, String> activeEventNames = new TreeMap<>();
    private final List<IndexEntry> pendingIndexEntries = new ArrayList<>();
    private final Map<Long, String> rawEventIdByEventId = new HashMap<>();
    @Getter
    private final Set<Long> recordOnlyEventIds;
    @Getter
    private final Set<String> recordOnlyRawEventIds;
    @Getter
    private final boolean filterActive;

    private final ScheduledExecutorService scheduler;
    private final BlockingQueue<PendingWrite> writeQueue = new ArrayBlockingQueue<>(WRITE_QUEUE_CAPACITY);
    private final Thread writerThread;

    private final AtomicLong messagesAccepted = new AtomicLong();
    private final AtomicLong messagesWritten = new AtomicLong();
    private final AtomicLong lastProcessedMessageId = new AtomicLong();

    private volatile String sessionId;
    private volatile String lastMessageArrivalTimestamp;
    private volatile boolean initialSyncComplete;
    private volatile boolean dirty;
    private volatile boolean closed;
    @Getter
    private volatile boolean writeQueueOverflowed;

    public TradingFeedSessionRecorder(TradingFeedReaderConfiguration configuration) throws IOException {
        this.configuration = configuration;
        this.recordOnlyEventIds = configuration.getRecordOnlyEventIds();
        this.recordOnlyRawEventIds = configuration.getRecordOnlyRawEventIds();
        this.filterActive = (recordOnlyEventIds != null && !recordOnlyEventIds.isEmpty())
                || (recordOnlyRawEventIds != null && !recordOnlyRawEventIds.isEmpty());
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.compactJsonWriter = this.objectMapper.writer().without(SerializationFeature.INDENT_OUTPUT);
        this.protobufPrinter = JsonFormat.printer()
                .alwaysPrintFieldsWithNoPresence()
                .preservingProtoFieldNames();

        this.sessionFolder = prepareSessionFolder(configuration.getSaveMessagesToFolder());
        this.messagesFolder = Files.createDirectories(sessionFolder.resolve(MESSAGES_FOLDER_NAME));
        this.subscriptionStatsFile = sessionFolder.resolve(SUBSCRIPTION_STATS_FILENAME);
        this.messagesIndexFile = sessionFolder.resolve(MESSAGES_INDEX_FILENAME);

        writeJsonAtomically(
                sessionFolder.resolve(SUBSCRIPTION_INFO_FILENAME),
                buildSubscriptionInfo()
        );

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleWithFixedDelay(this::flushSafely, 5, 5, TimeUnit.SECONDS);

        this.writerThread = new Thread(this::writerLoop, "tradingFeedRecorder-writer");
        this.writerThread.setDaemon(true);
        this.writerThread.start();
    }

    public long getMessagesAcceptedTotal() {
        return messagesAccepted.get();
    }

    public long getMessagesWrittenTotal() {
        return messagesWritten.get();
    }

    public synchronized Map<String, Long> getMessageTypeCountersSnapshot() {
        return new TreeMap<>(messageTypeCounters);
    }

    public void recordMessage(OddsmarketTradingDto.ServerMessage serverMessage, Instant arrivalTimestamp,
                              InMemoryStateStorage inMemoryStateStorage) {
        if (closed) {
            return;
        }

        updateRawEventIdCache(serverMessage);

        if (!shouldRecord(serverMessage)) {
            return;
        }

        String messageType = messageType(serverMessage);
        Long singleEventId = singleEventId(serverMessage);
        long messageId = serverMessage.getMessageId();
        String arrivalTimestampIso = ISO_INSTANT.format(arrivalTimestamp);
        String messageFileName = buildMessageFileName(singleEventId, messageId, messageType);

        synchronized (this) {
            if (closed) {
                return;
            }
            if (serverMessage.hasSessionStart()) {
                sessionId = serverMessage.getSessionStart().getSessionId();
            }
            if (serverMessage.hasInitialSyncComplete()) {
                initialSyncComplete = true;
            }
            refreshEventSummaries(inMemoryStateStorage);
            dirty = true;
        }

        messagesAccepted.incrementAndGet();

        PendingWrite pending = new PendingWrite(
                serverMessage,
                arrivalTimestampIso,
                messageType,
                singleEventId,
                messageId,
                messageFileName
        );
        if (!writeQueue.offer(pending)) {
            writeQueueOverflowed = true;
        }
    }

    private void writerLoop() {
        while (true) {
            PendingWrite pending;
            try {
                pending = writeQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (pending == POISON_PILL) {
                return;
            }
            try {
                writePending(pending);
            } catch (Exception e) {
                System.err.println("Failed to write message file: " + pending.messageFileName);
                e.printStackTrace();
            }
        }
    }

    private void writePending(PendingWrite pending) throws IOException {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put(ARRIVAL_TIMESTAMP_KEY, pending.arrivalTimestampIso);
        envelope.putRawValue(CONTENT_KEY, new RawValue(protobufPrinter.print(pending.serverMessage)));

        Path messageFile = messagesFolder.resolve(pending.messageFileName);
        long sizeBytes = writeJsonAtomically(messageFile, envelope);

        IndexEntry indexEntry = buildIndexEntry(
                pending.messageId,
                pending.messageType,
                pending.singleEventId,
                pending.arrivalTimestampIso,
                pending.messageFileName,
                sizeBytes
        );

        // Brief lock just to update shared state — no I/O held under the lock.
        synchronized (this) {
            messagesWritten.incrementAndGet();
            lastProcessedMessageId.set(pending.messageId);
            lastMessageArrivalTimestamp = pending.arrivalTimestampIso;
            messageTypeCounters.merge(pending.messageType, 1L, Long::sum);
            pendingIndexEntries.add(indexEntry);
            dirty = true;
        }
    }

    private static final class PendingWrite {
        final OddsmarketTradingDto.ServerMessage serverMessage;
        final String arrivalTimestampIso;
        final String messageType;
        final Long singleEventId;
        final long messageId;
        final String messageFileName;

        PendingWrite(
                OddsmarketTradingDto.ServerMessage serverMessage,
                String arrivalTimestampIso,
                String messageType,
                Long singleEventId,
                long messageId,
                String messageFileName
        ) {
            this.serverMessage = serverMessage;
            this.arrivalTimestampIso = arrivalTimestampIso;
            this.messageType = messageType;
            this.singleEventId = singleEventId;
            this.messageId = messageId;
            this.messageFileName = messageFileName;
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }

        // Signal the writer to drain pending entries and exit
        writeQueue.offer(POISON_PILL);
        try {
            writerThread.join(WRITER_SHUTDOWN_TIMEOUT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (writerThread.isAlive()) {
            System.err.println(
                    "Writer thread did not finish within " + WRITER_SHUTDOWN_TIMEOUT_MS + "ms; "
                            + writeQueue.size() + " messages still queued and may be lost."
            );
        }

        scheduler.shutdownNow();
        synchronized (this) {
            flush();
        }
    }

    private synchronized void flush() throws IOException {
        if (!dirty) {
            return;
        }
        appendPendingIndexEntries();
        writeJsonAtomically(subscriptionStatsFile, buildSubscriptionStats());
        dirty = false;
    }

    private void appendPendingIndexEntries() throws IOException {
        if (pendingIndexEntries.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (IndexEntry entry : pendingIndexEntries) {
            builder.append(compactJsonWriter.writeValueAsString(entry)).append('\n');
        }
        Files.writeString(
                messagesIndexFile,
                builder,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        pendingIndexEntries.clear();
    }

    private IndexEntry buildIndexEntry(
            long messageId,
            String messageType,
            Long eventId,
            String arrivalTimestampIso,
            String fileName,
            long sizeBytes
    ) {
        return IndexEntry.builder()
                .messageId(messageId)
                .type(messageType)
                .eventId(eventId)
                .arrivalTimestamp(arrivalTimestampIso)
                .fileName(fileName)
                .sizeBytes(sizeBytes)
                .build();
    }

    private void flushSafely() {
        try {
            flush();
        } catch (IOException e) {
            System.err.println("Failed to write " + SUBSCRIPTION_STATS_FILENAME);
            e.printStackTrace();
        }
    }

    private void refreshEventSummaries(InMemoryStateStorage inMemoryStateStorage) {
        activeEventNames.clear();
        inMemoryStateStorage.getEventByEventId().values().stream()
                .sorted(Comparator.comparingLong(InMemoryStateStorage.Event::getId))
                .forEach(event -> {
                    String eventName = event.getName();
                    activeEventNames.put(event.getId(), eventName);
                    seenEventNames.put(event.getId(), eventName);
                });
    }

    private SubscriptionInfo buildSubscriptionInfo() {
        List<Long> filterEventIds = (recordOnlyEventIds != null && !recordOnlyEventIds.isEmpty())
                ? sortedLongValues(recordOnlyEventIds)
                : null;
        List<String> filterRawEventIds = (recordOnlyRawEventIds != null && !recordOnlyRawEventIds.isEmpty())
                ? sortedStringValues(recordOnlyRawEventIds)
                : null;

        return SubscriptionInfo.builder()
                .feedDomain(configuration.getFeedDomain())
                .websocketUrl(toFeedWebsocketUrl(configuration.getFeedDomain()))
                .tradingFeedId(configuration.getTradingFeedId())
                .saveMessagesToFolder(configuration.getSaveMessagesToFolder().toAbsolutePath().toString())
                .groupMessagesByEvent(configuration.isGroupMessagesByEvent())
                .sessionFolder(sessionFolder.toAbsolutePath().toString())
                .sportIds(sortedShortValues(configuration.getSportIds()))
                .locales(sortedStringValues(configuration.getLocales()))
                .rawIdOriginBookmakerId(configuration.getRawIdOriginBookmakerId())
                .fillRawOutcomeId(configuration.getFillRawOutcomeId())
                .fillDirectLink(configuration.getFillDirectLink())
                .recordOnlyEventIds(filterEventIds)
                .recordOnlyRawEventIds(filterRawEventIds)
                .build();
    }

    private List<Long> sortedLongValues(Iterable<Long> values) {
        if (values == null) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (Long value : values) {
            result.add(value);
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }

    private SubscriptionStats buildSubscriptionStats() {
        return SubscriptionStats.builder()
                .updatedAt(ISO_INSTANT.format(Instant.now()))
                .messagesTotal(messagesWritten.get())
                .messagesAccepted(messagesAccepted.get())
                .lastProcessedMessageId(lastProcessedMessageId.get())
                .lastMessageArrivalTimestamp(lastMessageArrivalTimestamp)
                .sessionId(sessionId)
                .initialSyncComplete(initialSyncComplete)
                .activeEventsCount(activeEventNames.size())
                .seenEventsCount(seenEventNames.size())
                .messageTypeCounters(new TreeMap<>(messageTypeCounters))
                .activeEvents(toEventSummaries(activeEventNames))
                .seenEvents(toEventSummaries(seenEventNames))
                .build();
    }

    private List<EventSummary> toEventSummaries(Map<Long, String> eventNames) {
        List<EventSummary> result = new ArrayList<>();
        for (Map.Entry<Long, String> entry : eventNames.entrySet()) {
            result.add(new EventSummary(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private String buildMessageFileName(Long eventId, long messageId, String messageType) {
        String normalizedType = messageType.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        if (configuration.isGroupMessagesByEvent() && eventId != null) {
            return eventId + "_" + messageId + "_" + normalizedType + ".json";
        }
        if (eventId != null) {
            return messageId + "_" + eventId + "_" + normalizedType + ".json";
        }
        return messageId + "_" + normalizedType + ".json";
    }

    private boolean shouldRecord(OddsmarketTradingDto.ServerMessage serverMessage) {
        if (!filterActive) {
            return true;
        }
        switch (serverMessage.getPayloadCase()) {
            case EVENTSNAPSHOT: {
                OddsmarketTradingDto.EventSnapshot snapshot = serverMessage.getEventSnapshot();
                String rawEventId = snapshot.hasEventMetadata() ? snapshot.getEventMetadata().getRawEventId() : null;
                return matchesByEventId(snapshot.getEventId()) || matchesByRawEventId(rawEventId);
            }
            case EVENTPATCH: {
                OddsmarketTradingDto.EventPatch patch = serverMessage.getEventPatch();
                long eventId = patch.getEventId();
                if (matchesByEventId(eventId)) {
                    return true;
                }
                String rawEventId = patch.hasUpdatedEventMetadata()
                        ? patch.getUpdatedEventMetadata().getRawEventId()
                        : rawEventIdByEventId.get(eventId);
                return matchesByRawEventId(rawEventId);
            }
            case EVENTSREMOVED: {
                for (Long removedEventId : serverMessage.getEventsRemoved().getEventIdsList()) {
                    if (matchesByEventId(removedEventId)) {
                        return true;
                    }
                    if (matchesByRawEventId(rawEventIdByEventId.get(removedEventId))) {
                        return true;
                    }
                }
                return false;
            }
            case SESSIONSTART:
            case INITIALSYNCCOMPLETE:
            case HEARTBEAT:
            case ERRORMESSAGE:
            case PAYLOAD_NOT_SET:
            default:
                return false;
        }
    }

    private boolean matchesByEventId(long eventId) {
        return recordOnlyEventIds != null && recordOnlyEventIds.contains(eventId);
    }

    private boolean matchesByRawEventId(String rawEventId) {
        return recordOnlyRawEventIds != null
                && rawEventId != null
                && !rawEventId.isEmpty()
                && recordOnlyRawEventIds.contains(rawEventId);
    }

    private void updateRawEventIdCache(OddsmarketTradingDto.ServerMessage serverMessage) {
        if (serverMessage.hasEventSnapshot()) {
            OddsmarketTradingDto.EventSnapshot snapshot = serverMessage.getEventSnapshot();
            if (snapshot.hasEventMetadata()) {
                String rawEventId = snapshot.getEventMetadata().getRawEventId();
                if (!rawEventId.isEmpty()) {
                    rawEventIdByEventId.put(snapshot.getEventId(), rawEventId);
                }
            }
        } else if (serverMessage.hasEventPatch() && serverMessage.getEventPatch().hasUpdatedEventMetadata()) {
            OddsmarketTradingDto.EventPatch patch = serverMessage.getEventPatch();
            String rawEventId = patch.getUpdatedEventMetadata().getRawEventId();
            if (!rawEventId.isEmpty()) {
                rawEventIdByEventId.put(patch.getEventId(), rawEventId);
            }
        }
    }

    private Long singleEventId(OddsmarketTradingDto.ServerMessage serverMessage) {
        if (serverMessage.hasEventSnapshot()) {
            return serverMessage.getEventSnapshot().getEventId();
        }
        if (serverMessage.hasEventPatch()) {
            return serverMessage.getEventPatch().getEventId();
        }
        if (serverMessage.hasEventsRemoved() && serverMessage.getEventsRemoved().getEventIdsCount() == 1) {
            return serverMessage.getEventsRemoved().getEventIds(0);
        }
        return null;
    }

    public static String messageType(OddsmarketTradingDto.ServerMessage serverMessage) {
        switch (serverMessage.getPayloadCase()) {
            case SESSIONSTART:
                return SESSION_START_MESSAGE_TYPE;
            case EVENTSNAPSHOT:
                return EVENT_SNAPSHOT_MESSAGE_TYPE;
            case EVENTPATCH:
                return EVENT_PATCH_MESSAGE_TYPE;
            case EVENTSREMOVED:
                return EVENTS_REMOVED_MESSAGE_TYPE;
            case INITIALSYNCCOMPLETE:
                return INITIAL_SYNC_COMPLETE_MESSAGE_TYPE;
            case HEARTBEAT:
                return HEARTBEAT_MESSAGE_TYPE;
            case ERRORMESSAGE:
                return ERROR_MESSAGE_TYPE;
            case PAYLOAD_NOT_SET:
            default:
                return PAYLOAD_NOT_SET_MESSAGE_TYPE;
        }
    }

    private List<Short> sortedShortValues(Iterable<Short> values) {
        if (values == null) {
            return List.of();
        }
        List<Short> result = new ArrayList<>();
        for (Short value : values) {
            result.add(value);
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }

    private List<String> sortedStringValues(Iterable<String> values) {
        if (values == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            result.add(value);
        }
        result.sort(String::compareTo);
        return result;
    }

    private String toFeedWebsocketUrl(String feedDomain) {
        return (feedDomain.startsWith("localhost") ? "ws://" : "wss://") + feedDomain;
    }

    private Path prepareSessionFolder(Path baseFolder) throws IOException {
        Path result = baseFolder.toAbsolutePath().normalize().resolve(SESSION_FOLDER_NAME).normalize();
        if (!SESSION_FOLDER_NAME.equals(result.getFileName().toString())) {
            throw new IllegalStateException("Expected session folder to end with " + SESSION_FOLDER_NAME);
        }
        if (Files.exists(result)) {
            deleteRecursively(result);
        }
        return Files.createDirectories(result);
    }

    private void deleteRecursively(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private long writeJsonAtomically(Path targetFile, Object content) throws IOException {
        Files.createDirectories(targetFile.getParent());
        Path tempFile = targetFile.resolveSibling(targetFile.getFileName() + TEMP_FILE_SUFFIX);
        byte[] bytes = objectMapper.writeValueAsBytes(content);
        Files.write(tempFile, bytes);
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return bytes.length;
    }
}
