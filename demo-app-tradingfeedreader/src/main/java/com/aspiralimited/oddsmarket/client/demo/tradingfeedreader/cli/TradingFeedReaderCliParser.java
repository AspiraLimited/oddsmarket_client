package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.API_KEY_FILE_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.API_KEY_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.DEFAULT_SAVE_MESSAGES_FOLDER;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.DURATION_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FEED_DOMAIN_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_DIRECT_LINK_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_DIRECT_LINK_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_RAW_OUTCOME_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_RAW_OUTCOME_ID_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.GROUP_MESSAGES_BY_EVENT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.GROUP_MESSAGES_BY_EVENT_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INTERACTIVE_FLAG;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INTERACTIVE_MODE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.LOCALES_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.MAX_MESSAGES_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.OPTION_PREFIX;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RECORD_ONLY_EVENT_IDS_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RECORD_ONLY_RAW_EVENT_IDS_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SAVE_MESSAGES_TO_FOLDER_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SPORT_IDS_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.TRADING_FEED_ID_OPTION;

public class TradingFeedReaderCliParser {

    public boolean isInteractiveMode(String[] args) {
        return args.length == 1 && (INTERACTIVE_FLAG.equalsIgnoreCase(args[0]) || INTERACTIVE_MODE.equalsIgnoreCase(args[0]));
    }

    public boolean isHelpRequested(String[] args) {
        for (String arg : args) {
            if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    public TradingFeedReaderConfiguration parse(String[] args) {
        Map<String, String> options = parseOptions(args);

        String feedDomain = requireNonEmpty(
                options.get(FEED_DOMAIN_OPTION),
                "--feedDomain is required (e.g. --feedDomain=api-pr.oddsmarket.org)"
        );
        short tradingFeedId = parseRequiredShort(
                options.get(TRADING_FEED_ID_OPTION),
                "--tradingFeedId is required (e.g. --tradingFeedId=500)"
        );

        Set<Short> sportIds = CliValueParsers.parseShortSet(options.get(SPORT_IDS_OPTION));

        String literalApiKey = options.get(API_KEY_OPTION);
        Path apiKeyFile = options.containsKey(API_KEY_FILE_OPTION)
                ? Paths.get(requireNonEmpty(options.get(API_KEY_FILE_OPTION), "--apiKeyFile path must not be empty"))
                : null;
        String apiKey = ApiKeyResolver.resolve(literalApiKey, apiKeyFile);

        Path saveMessagesToFolder = options.containsKey(SAVE_MESSAGES_TO_FOLDER_OPTION)
                ? Paths.get(requireNonEmpty(options.get(SAVE_MESSAGES_TO_FOLDER_OPTION), "--saveMessagesToFolder path must not be empty"))
                : Paths.get(DEFAULT_SAVE_MESSAGES_FOLDER);

        boolean groupMessagesByEvent = options.containsKey(GROUP_MESSAGES_BY_EVENT_OPTION)
                && CliValueParsers.parseRequiredBoolean(GROUP_MESSAGES_BY_EVENT_KEY, options.get(GROUP_MESSAGES_BY_EVENT_OPTION));

        Set<String> locales = options.containsKey(LOCALES_OPTION)
                ? CliValueParsers.parseStringSet(options.get(LOCALES_OPTION))
                : null;

        Short rawIdOriginBookmakerId = options.containsKey(RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION)
                ? Short.parseShort(requireNonEmpty(options.get(RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION), "--rawIdOriginBookmakerId must not be empty"))
                : null;

        Boolean fillRawOutcomeId = options.containsKey(FILL_RAW_OUTCOME_ID_OPTION)
                ? CliValueParsers.parseRequiredBoolean(FILL_RAW_OUTCOME_ID_KEY, options.get(FILL_RAW_OUTCOME_ID_OPTION))
                : null;

        Boolean fillDirectLink = options.containsKey(FILL_DIRECT_LINK_OPTION)
                ? CliValueParsers.parseRequiredBoolean(FILL_DIRECT_LINK_KEY, options.get(FILL_DIRECT_LINK_OPTION))
                : null;

        Set<Long> recordOnlyEventIds = options.containsKey(RECORD_ONLY_EVENT_IDS_OPTION)
                ? CliValueParsers.parseLongSet(options.get(RECORD_ONLY_EVENT_IDS_OPTION))
                : null;

        Set<String> recordOnlyRawEventIds = options.containsKey(RECORD_ONLY_RAW_EVENT_IDS_OPTION)
                ? CliValueParsers.parseStringSet(options.get(RECORD_ONLY_RAW_EVENT_IDS_OPTION))
                : null;

        Duration duration = options.containsKey(DURATION_OPTION)
                ? CliValueParsers.parseDuration(options.get(DURATION_OPTION))
                : null;

        Long maxMessages = options.containsKey(MAX_MESSAGES_OPTION)
                ? CliValueParsers.parsePositiveLong(MAX_MESSAGES_OPTION, options.get(MAX_MESSAGES_OPTION))
                : null;

        return TradingFeedReaderConfiguration.builder()
                .feedDomain(feedDomain)
                .apiKey(apiKey)
                .tradingFeedId(tradingFeedId)
                .sportIds(sportIds)
                .saveMessagesToFolder(saveMessagesToFolder)
                .groupMessagesByEvent(groupMessagesByEvent)
                .locales(locales)
                .rawIdOriginBookmakerId(rawIdOriginBookmakerId)
                .fillRawOutcomeId(fillRawOutcomeId)
                .fillDirectLink(fillDirectLink)
                .recordOnlyEventIds(recordOnlyEventIds)
                .recordOnlyRawEventIds(recordOnlyRawEventIds)
                .duration(duration)
                .maxMessages(maxMessages)
                .build();
    }

    private Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            if (!argument.startsWith(OPTION_PREFIX)) {
                throw new IllegalArgumentException(
                        "All arguments must be named flags starting with '" + OPTION_PREFIX + "', got: '" + argument + "'"
                );
            }
            argument = argument.substring(OPTION_PREFIX.length());
            int separatorIndex = argument.indexOf('=');
            if (separatorIndex >= 0) {
                String key = argument.substring(0, separatorIndex).toLowerCase(Locale.ROOT);
                String value = argument.substring(separatorIndex + 1);
                options.put(key, value);
                continue;
            }
            String key = argument.toLowerCase(Locale.ROOT);
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for option: --" + argument);
            }
            options.put(key, args[++i]);
        }
        return options;
    }

    private short parseRequiredShort(String value, String missingMessage) {
        String nonEmpty = requireNonEmpty(value, missingMessage);
        try {
            return Short.parseShort(nonEmpty);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(missingMessage + " — got non-numeric value: '" + nonEmpty + "'", e);
        }
    }

    private String requireNonEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
