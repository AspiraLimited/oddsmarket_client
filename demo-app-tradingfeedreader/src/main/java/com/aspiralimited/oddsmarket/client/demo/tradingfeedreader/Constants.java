package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

public final class Constants {
    public static final String OPTION_PREFIX = "--";
    public static final String INTERACTIVE_FLAG = "--interactive";
    public static final String INTERACTIVE_MODE = "interactive";

    public static final String SESSION_FOLDER_NAME = "tradingFeedSessionData";
    public static final String SUBSCRIPTION_INFO_FILENAME = "subscriptionInfo.json";
    public static final String SUBSCRIPTION_STATS_FILENAME = "subscriptionStats.json";
    public static final String SUMMARY_FILENAME = "summary.json";
    public static final String MESSAGES_INDEX_FILENAME = "messagesIndex.jsonl";
    public static final String MESSAGES_FOLDER_NAME = "messages";
    public static final String TEMP_FILE_SUFFIX = ".tmp";

    public static final String ARRIVAL_TIMESTAMP_KEY = "arrivalTimestamp";
    public static final String CONTENT_KEY = "content";

    public static final String GROUP_MESSAGES_BY_EVENT_KEY = "groupMessagesByEvent";
    public static final String FILL_RAW_OUTCOME_ID_KEY = "fillRawOutcomeId";
    public static final String FILL_DIRECT_LINK_KEY = "fillDirectLink";

    public static final String FEED_DOMAIN_OPTION = "feeddomain";
    public static final String TRADING_FEED_ID_OPTION = "tradingfeedid";
    public static final String SPORT_IDS_OPTION = "sportids";
    public static final String SAVE_MESSAGES_TO_FOLDER_OPTION = "savemessagestofolder";
    public static final String GROUP_MESSAGES_BY_EVENT_OPTION = "groupmessagesbyevent";
    public static final String LOCALES_OPTION = "locales";
    public static final String RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION = "rawidoriginbookmakerid";
    public static final String FILL_RAW_OUTCOME_ID_OPTION = "fillrawoutcomeid";
    public static final String FILL_DIRECT_LINK_OPTION = "filldirectlink";
    public static final String RECORD_ONLY_EVENT_IDS_OPTION = "recordonlyeventids";
    public static final String RECORD_ONLY_RAW_EVENT_IDS_OPTION = "recordonlyraweventids";
    public static final String API_KEY_OPTION = "apikey";
    public static final String API_KEY_FILE_OPTION = "apikeyfile";
    public static final String DURATION_OPTION = "duration";
    public static final String MAX_MESSAGES_OPTION = "maxmessages";
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
