package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;


@Getter
@Builder
public class SessionSummary {
    private final int schemaVersion;
    private final String startedAt;
    private final String endedAt;
    private final long durationSeconds;
    private final ExitInfo exit;
    private final String sessionId;
    private final boolean initialSyncCompleted;
    private final String sessionFolder;
    private final MessagesSeen messagesSeen;
    private final MessagesRecorded messagesRecorded;
    private final int activeEventsAtEnd;
    private final int distinctEventsSeen;
    private final FilterSummary filter;
    private final Limits limits;

    @Getter
    @Builder
    public static class ExitInfo {
        private final int code;
        private final String reason;
        private final String fatalErrorCode;
    }

    @Getter
    @Builder
    public static class MessagesSeen {
        private final long total;
        private final Map<String, Long> byType;
    }

    @Getter
    @Builder
    public static class MessagesRecorded {
        /**
         * Number of messages actually written to disk.
         */
        private final long total;
        /**
         * Number of messages that passed the filter and were enqueued (≥ total in mid-session, == total after drain).
         */
        private final long accepted;
        private final Double percentOfSeen;
        private final Map<String, Long> byType;
    }

    @Getter
    @Builder
    public static class FilterSummary {
        private final boolean active;
        private final List<Long> recordOnlyEventIds;
        private final List<String> recordOnlyRawEventIds;
    }

    @Getter
    @Builder
    public static class Limits {
        private final Long durationSeconds;
        private final Long maxMessages;
    }
}
