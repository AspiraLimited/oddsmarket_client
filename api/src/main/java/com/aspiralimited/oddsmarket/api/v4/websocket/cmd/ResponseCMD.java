package com.aspiralimited.oddsmarket.api.v4.websocket.cmd;

import com.aspiralimited.oddsmarket.api.ApiVersion;

public enum ResponseCMD {
    AUTHORIZED, SUBSCRIBED, UNSUBSCRIBED, FIELDS, BOOKMAKER_EVENTS, OUTCOMES, BOOKMAKER_EVENTS_REMOVED, PONG, ERROR, UNKNOWN, INITIAL_STATE_TRANSFERRED;

    public String getName(ApiVersion version) {
        if (version.ordinal() < ApiVersion.V4.ordinal()) {
            if (this == BOOKMAKER_EVENTS) {
                return "bookmakerevents";
            }
            if (this == OUTCOMES) {
                return "odds";
            }
            if (this == BOOKMAKER_EVENTS_REMOVED) {
                return "removed";
            }
            return name().toLowerCase();
        }
        return name().toLowerCase();
    }

    @Override
    @Deprecated
    public String toString() {
        throw new IllegalStateException("deprecated!");
    }
}
