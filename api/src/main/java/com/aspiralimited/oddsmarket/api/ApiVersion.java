package com.aspiralimited.oddsmarket.api;

public enum ApiVersion {
    V1, V2, V3, V4;
    public static final ApiVersion LATEST = V4;
    public static final String LATEST_VERSION_URL_PREFIX = "/v4";
}
