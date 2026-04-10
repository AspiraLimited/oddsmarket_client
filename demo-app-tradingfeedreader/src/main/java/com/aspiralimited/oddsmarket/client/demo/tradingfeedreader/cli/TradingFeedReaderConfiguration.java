package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.Set;

@Getter
@RequiredArgsConstructor
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
}
