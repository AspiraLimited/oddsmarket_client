package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndexEntry {
    private final long messageId;
    private final String type;
    private final Long eventId;
    private final String arrivalTimestamp;
    private final String fileName;
    private final long sizeBytes;
}
