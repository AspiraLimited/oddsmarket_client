package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventSummary {
    private final long eventId;
    private final String name;
}
