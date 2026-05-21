package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model.InMemoryStateStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.util.JsonFormat;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.ACTIVE_EVENTS_COUNT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.ACTIVE_EVENTS_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.ARRIVAL_TIMESTAMP_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.CONTENT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.ERROR_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.EVENTS_REMOVED_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.EVENT_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.EVENT_PATCH_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.EVENT_SNAPSHOT_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FEED_DOMAIN_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILE_NAME_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_DIRECT_LINK_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_RAW_OUTCOME_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.GROUP_MESSAGES_BY_EVENT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.HEARTBEAT_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INITIAL_SYNC_COMPLETE_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INITIAL_SYNC_COMPLETE_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.LAST_MESSAGE_ARRIVAL_TIMESTAMP_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.LAST_PROCESSED_MESSAGE_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.LOCALES_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGES_FOLDER_NAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGES_INDEX_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGES_TOTAL_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGE_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGE_TYPE_COUNTERS_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.NAME_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.PAYLOAD_NOT_SET_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RAW_ID_ORIGIN_BOOKMAKER_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RECORD_ONLY_EVENT_IDS_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RECORD_ONLY_RAW_EVENT_IDS_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SAVE_MESSAGES_TO_FOLDER_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SEEN_EVENTS_COUNT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SEEN_EVENTS_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_FOLDER_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_FOLDER_NAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_START_MESSAGE_TYPE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SIZE_BYTES_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SPORT_IDS_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SUBSCRIPTION_INFO_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SUBSCRIPTION_STATS_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.TEMP_FILE_SUFFIX;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.TRADING_FEED_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.TYPE_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.UPDATED_AT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.WEBSOCKET_URL_KEY;

public class TradingFeedSessionRecorder implements Closeable {
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final TradingFeedReaderConfiguration configuration;
    private final Path sessionFolder;
    private final Path messagesFolder;
    private final Path subscriptionInfoFile;
    private final Path subscriptionStatsFile;
    private final Path messagesIndexFile;
    private final ObjectMapper objectMapper;
    private final ObjectWriter compactJsonWriter;
    private final JsonFormat.Printer protobufPrinter;
    private final ScheduledExecutorService scheduler;

    private final Map<String, Long> messageTypeCounters = new TreeMap<>();
    private final Map<Long, String> seenEventNames = new TreeMap<>();
    private final Map<Long, String> activeEventNames = new TreeMap<>();
    private final List<ObjectNode> pendingIndexEntries = new ArrayList<>();
    private final Map<Long, String> rawEventIdByEventId = new HashMap<>();
    private final Set<Long> recordOnlyEventIds;
    private final Set<String> recordOnlyRawEventIds;
    private final boolean filterActive;

    private long messagesTotal;
    private long lastProcessedMessageId;
    private String sessionId;
    private String lastMessageArrivalTimestamp;
    private boolean initialSyncComplete;
    private boolean dirty;
    private boolean closed;

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
        this.subscriptionInfoFile = sessionFolder.resolve(SUBSCRIPTION_INFO_FILENAME);
        this.subscriptionStatsFile = sessionFolder.resolve(SUBSCRIPTION_STATS_FILENAME);
        this.messagesIndexFile = sessionFolder.resolve(MESSAGES_INDEX_FILENAME);

        writeJsonAtomically(subscriptionInfoFile, buildSubscriptionInfoNode());

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleWithFixedDelay(this::flushSafely, 5, 5, TimeUnit.SECONDS);
    }

    public Path getSessionFolder() {
        return sessionFolder;
    }

    public synchronized long getMessagesRecordedTotal() {
        return messagesTotal;
    }

    public synchronized Map<String, Long> getMessageTypeCountersSnapshot() {
        return new TreeMap<>(messageTypeCounters);
    }

    public boolean isFilterActive() {
        return filterActive;
    }

    public Set<Long> getRecordOnlyEventIds() {
        return recordOnlyEventIds;
    }

    public Set<String> getRecordOnlyRawEventIds() {
        return recordOnlyRawEventIds;
    }

    public synchronized void recordMessage(
            OddsmarketTradingDto.ServerMessage serverMessage,
            Instant arrivalTimestamp,
            InMemoryStateStorage inMemoryStateStorage
    ) throws IOException {
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

        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put(ARRIVAL_TIMESTAMP_KEY, arrivalTimestampIso);
        JsonNode contentNode = objectMapper.readTree(protobufPrinter.print(serverMessage));
        envelope.set(CONTENT_KEY, contentNode);

        String messageFileName = buildMessageFileName(singleEventId, messageId, messageType);
        Path messageFile = messagesFolder.resolve(messageFileName);
        long messageFileSizeBytes = writeJsonAtomically(messageFile, envelope);

        pendingIndexEntries.add(buildIndexEntry(messageId, messageType, singleEventId, arrivalTimestampIso, messageFileName, messageFileSizeBytes));

        messagesTotal++;
        lastProcessedMessageId = messageId;
        lastMessageArrivalTimestamp = arrivalTimestampIso;
        messageTypeCounters.merge(messageType, 1L, Long::sum);

        if (serverMessage.hasSessionStart()) {
            sessionId = serverMessage.getSessionStart().getSessionId();
        }
        if (serverMessage.hasInitialSyncComplete()) {
            initialSyncComplete = true;
        }

        refreshEventSummaries(inMemoryStateStorage);
        dirty = true;
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        scheduler.shutdownNow();
        flush();
    }

    private synchronized void flush() throws IOException {
        if (!dirty) {
            return;
        }
        appendPendingIndexEntries();
        writeJsonAtomically(subscriptionStatsFile, buildSubscriptionStatsNode());
        dirty = false;
    }

    private void appendPendingIndexEntries() throws IOException {
        if (pendingIndexEntries.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (ObjectNode entry : pendingIndexEntries) {
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

    private ObjectNode buildIndexEntry(
            long messageId,
            String messageType,
            Long eventId,
            String arrivalTimestampIso,
            String fileName,
            long sizeBytes
    ) {
        ObjectNode entry = objectMapper.createObjectNode();
        entry.put(MESSAGE_ID_KEY, messageId);
        entry.put(TYPE_KEY, messageType);
        if (eventId != null) {
            entry.put(EVENT_ID_KEY, eventId);
        } else {
            entry.putNull(EVENT_ID_KEY);
        }
        entry.put(ARRIVAL_TIMESTAMP_KEY, arrivalTimestampIso);
        entry.put(FILE_NAME_KEY, fileName);
        entry.put(SIZE_BYTES_KEY, sizeBytes);
        return entry;
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

    private ObjectNode buildSubscriptionInfoNode() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put(FEED_DOMAIN_KEY, configuration.getFeedDomain());
        root.put(WEBSOCKET_URL_KEY, toFeedWebsocketUrl(configuration.getFeedDomain()));
        root.put(TRADING_FEED_ID_KEY, configuration.getTradingFeedId());
        root.put(SAVE_MESSAGES_TO_FOLDER_KEY, configuration.getSaveMessagesToFolder().toAbsolutePath().toString());
        root.put(GROUP_MESSAGES_BY_EVENT_KEY, configuration.isGroupMessagesByEvent());
        root.put(SESSION_FOLDER_KEY, sessionFolder.toAbsolutePath().toString());

        root.set(SPORT_IDS_KEY, objectMapper.valueToTree(sortedShortValues(configuration.getSportIds())));
        root.set(LOCALES_KEY, objectMapper.valueToTree(sortedStringValues(configuration.getLocales())));

        if (configuration.getRawIdOriginBookmakerId() != null) {
            root.put(RAW_ID_ORIGIN_BOOKMAKER_ID_KEY, configuration.getRawIdOriginBookmakerId());
        } else {
            root.putNull(RAW_ID_ORIGIN_BOOKMAKER_ID_KEY);
        }

        if (configuration.getFillRawOutcomeId() != null) {
            root.put(FILL_RAW_OUTCOME_ID_KEY, configuration.getFillRawOutcomeId());
        } else {
            root.putNull(FILL_RAW_OUTCOME_ID_KEY);
        }

        if (configuration.getFillDirectLink() != null) {
            root.put(FILL_DIRECT_LINK_KEY, configuration.getFillDirectLink());
        } else {
            root.putNull(FILL_DIRECT_LINK_KEY);
        }

        if (recordOnlyEventIds != null && !recordOnlyEventIds.isEmpty()) {
            root.set(RECORD_ONLY_EVENT_IDS_KEY, objectMapper.valueToTree(sortedLongValues(recordOnlyEventIds)));
        } else {
            root.putNull(RECORD_ONLY_EVENT_IDS_KEY);
        }
        if (recordOnlyRawEventIds != null && !recordOnlyRawEventIds.isEmpty()) {
            root.set(RECORD_ONLY_RAW_EVENT_IDS_KEY, objectMapper.valueToTree(sortedStringValues(recordOnlyRawEventIds)));
        } else {
            root.putNull(RECORD_ONLY_RAW_EVENT_IDS_KEY);
        }

        return root;
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

    private ObjectNode buildSubscriptionStatsNode() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put(UPDATED_AT_KEY, ISO_INSTANT.format(Instant.now()));
        root.put(MESSAGES_TOTAL_KEY, messagesTotal);
        root.put(LAST_PROCESSED_MESSAGE_ID_KEY, lastProcessedMessageId);
        if (lastMessageArrivalTimestamp != null) {
            root.put(LAST_MESSAGE_ARRIVAL_TIMESTAMP_KEY, lastMessageArrivalTimestamp);
        } else {
            root.putNull(LAST_MESSAGE_ARRIVAL_TIMESTAMP_KEY);
        }
        if (sessionId != null) {
            root.put(SESSION_ID_KEY, sessionId);
        } else {
            root.putNull(SESSION_ID_KEY);
        }
        root.put(INITIAL_SYNC_COMPLETE_KEY, initialSyncComplete);
        root.put(ACTIVE_EVENTS_COUNT_KEY, activeEventNames.size());
        root.put(SEEN_EVENTS_COUNT_KEY, seenEventNames.size());
        root.set(MESSAGE_TYPE_COUNTERS_KEY, objectMapper.valueToTree(messageTypeCounters));
        root.set(ACTIVE_EVENTS_KEY, objectMapper.valueToTree(toEventSummaries(activeEventNames)));
        root.set(SEEN_EVENTS_KEY, objectMapper.valueToTree(toEventSummaries(seenEventNames)));
        return root;
    }

    private List<Map<String, Object>> toEventSummaries(Map<Long, String> eventNames) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, String> entry : eventNames.entrySet()) {
            Map<String, Object> eventSummary = new LinkedHashMap<>();
            eventSummary.put(EVENT_ID_KEY, entry.getKey());
            eventSummary.put(NAME_KEY, entry.getValue());
            result.add(eventSummary);
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

    private long writeJsonAtomically(Path targetFile, JsonNode content) throws IOException {
        Files.createDirectories(targetFile.getParent());
        Path tempFile = targetFile.resolveSibling(targetFile.getFileName() + TEMP_FILE_SUFFIX);
        byte[] bytes = objectMapper.writeValueAsString(content).getBytes(StandardCharsets.UTF_8);
        Files.write(tempFile, bytes);
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return bytes.length;
    }
}
