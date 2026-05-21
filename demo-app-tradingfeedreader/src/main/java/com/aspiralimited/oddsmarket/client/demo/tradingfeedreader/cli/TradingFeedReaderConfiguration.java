package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
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
}
