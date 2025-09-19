package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Data
public class TradingFeedSubscriptionConfig {
    private String apiKey;
    private Short tradingFeedId;
    /**
     * Comma-separated list of sport IDs that the subscriber wants to receive.
     * If left empty, the feed is not filtered by sport.
     */
    private Set<Short> sportIds;
    /**
     * Comma-separated list of locale codes in ISO 639-1 format
     */
    private Set<String> locales;
    /**
     * ID of the raw IDs feed.
     * The specified bookmaker will be used as the source for rawEventId and rawOutcomeId.
     */
    private Short rawIdOriginBookmakerId;
    /**
     * Set to true to populate the rawOutcomeId field
     */
    private Boolean fillRawOutcomeId;
    /**
     * Set to true to populate the directLink field for EventMetaData and OutcomeData
     */
    private Boolean fillDirectLink;

    public String toQueryString() {
        String result = "";
        if (apiKey == null) {
            throw new IllegalStateException("apiKey must be set");
        }
        result = result + "apiKey=" + apiKey;
        if (tradingFeedId == null) {
            throw new IllegalStateException("tradingFeedId must be set");
        }
        result = result + "&tradingFeedId=" + tradingFeedId;
        if (sportIds != null) {
            result = result + "&sportIds=" + sportIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (locales != null) {
            result = result + "&locales=" + String.join(",", locales);
        }
        if (rawIdOriginBookmakerId != null) {
            result = result + "&rawIdOriginBookmakerId=" + rawIdOriginBookmakerId;
        }
        if (fillRawOutcomeId != null) {
            result = result + "&fillRawOutcomeId=" + fillRawOutcomeId;
        }
        if (fillDirectLink != null) {
            result = result + "&fillDirectLink=true";
        }
        return result;
    }
}
