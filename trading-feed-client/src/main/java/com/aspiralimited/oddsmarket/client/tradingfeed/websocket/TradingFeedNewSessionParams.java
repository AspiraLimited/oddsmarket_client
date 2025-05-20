package com.aspiralimited.oddsmarket.client.tradingfeed.websocket;

import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Data
public class TradingFeedNewSessionParams {
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
     * Time window (in seconds) during which the server stores messages for resumption.
     * If not specified or non-positive, then resume is not supported for this session.
     * Maximum: 60 seconds for live feeds, 240 seconds for prematch feeds.
     */
    private Integer resumeBufferLimitSeconds;
    private Boolean json;
    private Integer resumeRetryInterval;
    private Integer newSessionRetryInterval;

    public String toQueryString() {
        String result = "";
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
        if (resumeBufferLimitSeconds != null) {
            result = result + "&resumeBufferLimitSeconds=" + resumeBufferLimitSeconds;
        }
        if (json != null) {
            result = result + "&json=" + json;
        }
        return result;
    }
}
