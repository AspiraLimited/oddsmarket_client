package com.aspiralimited.oddsmarket.client.cmd;

public enum RequestCMD {
    AUTHORIZATION, SUBSCRIBE, PING, ERROR, UNKNOWN, UNSUBSCRIBE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
