package com.aspiralimited.oddsmarket.api.websocket.cmd;

import com.aspiralimited.oddsmarket.api.ApiVersion;

public enum RequestCMD {
    AUTHORIZATION, SUBSCRIBE, PING, UNSUBSCRIBE;

    public String getName(ApiVersion version) {
        return toString();
    }

    @Override
    @Deprecated
    public String toString() {
        throw new IllegalStateException("deprecated!");
    }
}
