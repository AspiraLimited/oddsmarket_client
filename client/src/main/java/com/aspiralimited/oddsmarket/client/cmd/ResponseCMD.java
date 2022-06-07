package com.aspiralimited.oddsmarket.client.cmd;

public enum ResponseCMD {
    AUTHORIZED, SUBSCRIBED, FIELDS, BookmakerEvents, Odds, REMOVED, PONG, ERROR, UNKNOWN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
