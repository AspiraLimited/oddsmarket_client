package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model;

public enum Node {
    LIVE, PREMATCH;

    public static Node parse(String host) {
        if (host.contains("api-pr.oddsmarket.org")) {
            return PREMATCH;
        } else {
            return LIVE;
        }
    }
}
