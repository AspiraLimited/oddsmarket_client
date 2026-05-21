package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

public final class Constants {
    public static final String OPTION_PREFIX = "--";
    public static final String INTERACTIVE_FLAG = "--interactive";
    public static final String INTERACTIVE_MODE = "interactive";
    public static final String POSITIONAL_SPORT_IDS_REGEX = "^\\d+(,\\d+)*$";

    public static final String SESSION_FOLDER_NAME = "tradingFeedSessionData";
    public static final String SUBSCRIPTION_INFO_FILENAME = "subscriptionInfo.json";
    public static final String SUBSCRIPTION_STATS_FILENAME = "subscriptionStats.json";
    public static final String MESSAGES_INDEX_FILENAME = "messagesIndex.jsonl";
    public static final String MESSAGES_FOLDER_NAME = "messages";
    public static final String TEMP_FILE_SUFFIX = ".tmp";

    public static final String FEED_DOMAIN_KEY = "feedDomain";
    public static final String WEBSOCKET_URL_KEY = "websocketUrl";
    public static final String TRADING_FEED_ID_KEY = "tradingFeedId";
    public static final String SAVE_MESSAGES_TO_FOLDER_KEY = "saveMessagesToFolder";
    public static final String GROUP_MESSAGES_BY_EVENT_KEY = "groupMessagesByEvent";
    public static final String SESSION_FOLDER_KEY = "sessionFolder";
    public static final String SPORT_IDS_KEY = "sportIds";
    public static final String LOCALES_KEY = "locales";
    public static final String RAW_ID_ORIGIN_BOOKMAKER_ID_KEY = "rawIdOriginBookmakerId";
    public static final String FILL_RAW_OUTCOME_ID_KEY = "fillRawOutcomeId";
    public static final String FILL_DIRECT_LINK_KEY = "fillDirectLink";
    public static final String ARRIVAL_TIMESTAMP_KEY = "arrivalTimestamp";
    public static final String CONTENT_KEY = "content";
    public static final String UPDATED_AT_KEY = "updatedAt";
    public static final String MESSAGES_TOTAL_KEY = "messagesTotal";
    public static final String LAST_PROCESSED_MESSAGE_ID_KEY = "lastProcessedMessageId";
    public static final String LAST_MESSAGE_ARRIVAL_TIMESTAMP_KEY = "lastMessageArrivalTimestamp";
    public static final String SESSION_ID_KEY = "sessionId";
    public static final String INITIAL_SYNC_COMPLETE_KEY = "initialSyncComplete";
    public static final String ACTIVE_EVENTS_COUNT_KEY = "activeEventsCount";
    public static final String SEEN_EVENTS_COUNT_KEY = "seenEventsCount";
    public static final String MESSAGE_TYPE_COUNTERS_KEY = "messageTypeCounters";
    public static final String ACTIVE_EVENTS_KEY = "activeEvents";
    public static final String SEEN_EVENTS_KEY = "seenEvents";
    public static final String EVENT_ID_KEY = "eventId";
    public static final String NAME_KEY = "name";
    public static final String MESSAGE_ID_KEY = "messageId";
    public static final String TYPE_KEY = "type";
    public static final String FILE_NAME_KEY = "fileName";
    public static final String SIZE_BYTES_KEY = "sizeBytes";

    public static final String SAVE_MESSAGES_TO_FOLDER_OPTION = "savemessagestofolder";
    public static final String GROUP_MESSAGES_BY_EVENT_OPTION = "groupmessagesbyevent";
    public static final String RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION = "rawidoriginbookmakerid";
    public static final String FILL_RAW_OUTCOME_ID_OPTION = "fillrawoutcomeid";
    public static final String FILL_DIRECT_LINK_OPTION = "filldirectlink";

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
