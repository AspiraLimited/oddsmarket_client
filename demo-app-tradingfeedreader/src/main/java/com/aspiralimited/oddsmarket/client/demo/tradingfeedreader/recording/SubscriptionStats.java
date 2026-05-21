package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class SubscriptionStats {
    private final String updatedAt;
    private final long messagesTotal;
    private final long messagesAccepted;
    private final long lastProcessedMessageId;
    private final String lastMessageArrivalTimestamp;
    private final String sessionId;
    private final boolean initialSyncComplete;
    private final int activeEventsCount;
    private final int seenEventsCount;
    private final Map<String, Long> messageTypeCounters;
    private final List<EventSummary> activeEvents;
    private final List<EventSummary> seenEvents;
}
