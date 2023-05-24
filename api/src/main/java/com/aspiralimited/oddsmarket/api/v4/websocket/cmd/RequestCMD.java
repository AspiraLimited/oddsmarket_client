package com.aspiralimited.oddsmarket.api.v4.websocket.cmd;

public enum RequestCMD {
    AUTHORIZATION, SUBSCRIBE, PING, UNSUBSCRIBE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
