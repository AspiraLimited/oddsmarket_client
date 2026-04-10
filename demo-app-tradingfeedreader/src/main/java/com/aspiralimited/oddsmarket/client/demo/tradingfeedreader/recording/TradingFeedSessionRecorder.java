package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model.InMemoryStateStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FEED_DOMAIN_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_DIRECT_LINK_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_RAW_OUTCOME_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.LOCALES_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MESSAGES_FOLDER_NAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RAW_ID_ORIGIN_BOOKMAKER_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SAVE_MESSAGES_TO_FOLDER_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_FOLDER_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SESSION_FOLDER_NAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SPORT_IDS_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SUBSCRIPTION_INFO_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SUBSCRIPTION_STATS_FILENAME;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.TRADING_FEED_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.WEBSOCKET_URL_KEY;

public class TradingFeedSessionRecorder implements Closeable {
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final TradingFeedReaderConfiguration configuration;
    private final Path sessionFolder;
    private final Path messagesFolder;
    private final Path subscriptionInfoFile;
    private final Path subscriptionStatsFile;
    private final ObjectMapper objectMapper;
    private final JsonFormat.Printer protobufPrinter;
    private final ScheduledExecutorService scheduler;

    private final Map<String, Long> messageTypeCounters = new TreeMap<>();
    private final Map<Long, String> seenEventNames = new TreeMap<>();
    private final Map<Long, String> activeEventNames = new TreeMap<>();

    private long messagesTotal;
    private long lastProcessedMessageId;
    private String sessionId;
    private String lastMessageArrivalTimestamp;
    private boolean initialSyncComplete;
    private boolean dirty;
    private boolean closed;

    public TradingFeedSessionRecorder(TradingFeedReaderConfiguration configuration) throws IOException {
        this.configuration = configuration;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.protobufPrinter = JsonFormat.printer()
                .alwaysPrintFieldsWithNoPresence()
                .preservingProtoFieldNames();

        this.sessionFolder = prepareSessionFolder(configuration.getSaveMessagesToFolder());
        this.messagesFolder = Files.createDirectories(sessionFolder.resolve(MESSAGES_FOLDER_NAME));
        this.subscriptionInfoFile = sessionFolder.resolve(SUBSCRIPTION_INFO_FILENAME);
        this.subscriptionStatsFile = sessionFolder.resolve(SUBSCRIPTION_STATS_FILENAME);

        writeJsonAtomically(subscriptionInfoFile, buildSubscriptionInfoNode());

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleWithFixedDelay(this::flushSafely, 5, 5, TimeUnit.SECONDS);
    }

    public synchronized void recordMessage(
            OddsmarketTradingDto.ServerMessage serverMessage,
            Instant arrivalTimestamp,
            InMemoryStateStorage inMemoryStateStorage
    ) throws IOException {
        if (closed) {
            return;
        }

        String messageType = messageType(serverMessage);
        Long singleEventId = singleEventId(serverMessage);
        long messageId = serverMessage.getMessageId();
        String arrivalTimestampIso = ISO_INSTANT.format(arrivalTimestamp);

        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("arrivalTimestamp", arrivalTimestampIso);
        JsonNode contentNode = objectMapper.readTree(protobufPrinter.print(serverMessage));
        envelope.set("content", contentNode);

        Path messageFile = messagesFolder.resolve(buildMessageFileName(singleEventId, messageId, messageType));
        writeJsonAtomically(messageFile, envelope);

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
        writeJsonAtomically(subscriptionStatsFile, buildSubscriptionStatsNode());
        dirty = false;
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

        return root;
    }

    private ObjectNode buildSubscriptionStatsNode() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("updatedAt", ISO_INSTANT.format(Instant.now()));
        root.put("messagesTotal", messagesTotal);
        root.put("lastProcessedMessageId", lastProcessedMessageId);
        if (lastMessageArrivalTimestamp != null) {
            root.put("lastMessageArrivalTimestamp", lastMessageArrivalTimestamp);
        } else {
            root.putNull("lastMessageArrivalTimestamp");
        }
        if (sessionId != null) {
            root.put("sessionId", sessionId);
        } else {
            root.putNull("sessionId");
        }
        root.put("initialSyncComplete", initialSyncComplete);
        root.put("activeEventsCount", activeEventNames.size());
        root.put("seenEventsCount", seenEventNames.size());
        root.set("messageTypeCounters", objectMapper.valueToTree(messageTypeCounters));
        root.set("activeEvents", objectMapper.valueToTree(toEventSummaries(activeEventNames)));
        root.set("seenEvents", objectMapper.valueToTree(toEventSummaries(seenEventNames)));
        return root;
    }

    private List<Map<String, Object>> toEventSummaries(Map<Long, String> eventNames) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, String> entry : eventNames.entrySet()) {
            Map<String, Object> eventSummary = new LinkedHashMap<>();
            eventSummary.put("eventId", entry.getKey());
            eventSummary.put("name", entry.getValue());
            result.add(eventSummary);
        }
        return result;
    }

    private String buildMessageFileName(Long eventId, long messageId, String messageType) {
        String normalizedType = messageType.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        if (eventId != null) {
            return eventId + "_" + messageId + "_" + normalizedType + ".json";
        }
        return messageId + "_" + normalizedType + ".json";
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

    private String messageType(OddsmarketTradingDto.ServerMessage serverMessage) {
        switch (serverMessage.getPayloadCase()) {
            case SESSIONSTART:
                return "sessionStart";
            case EVENTSNAPSHOT:
                return "eventSnapshot";
            case EVENTPATCH:
                return "eventPatch";
            case EVENTSREMOVED:
                return "eventsRemoved";
            case INITIALSYNCCOMPLETE:
                return "initialSyncComplete";
            case HEARTBEAT:
                return "heartbeat";
            case ERRORMESSAGE:
                return "errorMessage";
            case PAYLOAD_NOT_SET:
            default:
                return "payloadNotSet";
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

    private void writeJsonAtomically(Path targetFile, JsonNode content) throws IOException {
        Files.createDirectories(targetFile.getParent());
        Path tempFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
        Files.writeString(tempFile, objectMapper.writeValueAsString(content), StandardCharsets.UTF_8);
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
