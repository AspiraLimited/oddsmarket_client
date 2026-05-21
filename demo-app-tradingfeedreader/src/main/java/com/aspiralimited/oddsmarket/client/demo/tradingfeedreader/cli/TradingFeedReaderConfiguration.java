package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

@Getter
@Builder
public class TradingFeedReaderConfiguration {
    private final String feedDomain;
    private final String apiKey;
    private final short tradingFeedId;
    private final Set<Short> sportIds;
    private final Path saveMessagesToFolder;
    private final boolean groupMessagesByEvent;
    private final Set<String> locales;
    private final Short rawIdOriginBookmakerId;
    private final Boolean fillRawOutcomeId;
    private final Boolean fillDirectLink;
    private final Set<Long> recordOnlyEventIds;
    private final Set<String> recordOnlyRawEventIds;
    /** Maximum session duration. If null, runs until Ctrl+C or fatal error. */
    private final Duration duration;
    /** Maximum number of messages recorded to disk. If null, unlimited. */
    private final Long maxMessages;
}
