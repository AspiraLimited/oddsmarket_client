package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

public final class Constants {

    public static final String SESSION_FOLDER_NAME = "tradingFeedSessionData";
    public static final String SUBSCRIPTION_INFO_FILENAME = "subscriptionInfo.json";
    public static final String SUBSCRIPTION_STATS_FILENAME = "subscriptionStats.json";
    public static final String SUMMARY_FILENAME = "summary.json";
    public static final String MESSAGES_INDEX_FILENAME = "messagesIndex.jsonl";
    public static final String MESSAGES_FOLDER_NAME = "messages";
    public static final String TEMP_FILE_SUFFIX = ".tmp";

    public static final String ARRIVAL_TIMESTAMP_KEY = "arrivalTimestamp";
    public static final String CONTENT_KEY = "content";

    public static final String DEFAULT_API_KEY_FILE = "api-key.txt";
    public static final String DEFAULT_SAVE_MESSAGES_FOLDER = "data";
    public static final String INTERACTIVE_PASTE_API_KEY_VALUE = "paste";

    public static final String SESSION_START_MESSAGE_TYPE = "sessionStart";
    public static final String EVENT_SNAPSHOT_MESSAGE_TYPE = "eventSnapshot";
    public static final String EVENT_PATCH_MESSAGE_TYPE = "eventPatch";
    public static final String EVENTS_REMOVED_MESSAGE_TYPE = "eventsRemoved";
    public static final String INITIAL_SYNC_COMPLETE_MESSAGE_TYPE = "initialSyncComplete";
    public static final String HEARTBEAT_MESSAGE_TYPE = "heartbeat";
    public static final String ERROR_MESSAGE_TYPE = "errorMessage";
    public static final String PAYLOAD_NOT_SET_MESSAGE_TYPE = "payloadNotSet";

    private Constants() {
    }
}
