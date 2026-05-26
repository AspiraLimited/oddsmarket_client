package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.recording;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SubscriptionInfo {
    private final String feedDomain;
    private final String websocketUrl;
    private final short tradingFeedId;
    private final String saveMessagesToFolder;
    private final boolean groupMessagesByEvent;
    private final String sessionFolder;
    private final List<Short> sportIds;
    private final List<String> locales;
    private final Short rawIdOriginBookmakerId;
    private final Boolean fillRawOutcomeId;
    private final Boolean fillDirectLink;
    private final List<Long> recordOnlyEventIds;
    private final List<String> recordOnlyRawEventIds;
}
