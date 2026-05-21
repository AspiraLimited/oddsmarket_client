package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.API_KEY_FILE_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.API_KEY_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.DEFAULT_SAVE_MESSAGES_FOLDER;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_DIRECT_LINK_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_DIRECT_LINK_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_RAW_OUTCOME_ID_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.FILL_RAW_OUTCOME_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.GROUP_MESSAGES_BY_EVENT_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.GROUP_MESSAGES_BY_EVENT_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INTERACTIVE_FLAG;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INTERACTIVE_MODE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.LOCALES_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.OPTION_PREFIX;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.POSITIONAL_SPORT_IDS_REGEX;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RAW_ID_ORIGIN_BOOKMAKER_ID_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RECORD_ONLY_EVENT_IDS_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.RECORD_ONLY_RAW_EVENT_IDS_OPTION;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SAVE_MESSAGES_TO_FOLDER_KEY;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.SAVE_MESSAGES_TO_FOLDER_OPTION;

public class TradingFeedReaderCliParser {

    public boolean isInteractiveMode(String[] args) {
        return args.length == 1 && (INTERACTIVE_FLAG.equalsIgnoreCase(args[0]) || INTERACTIVE_MODE.equalsIgnoreCase(args[0]));
    }

    public TradingFeedReaderConfiguration parse(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Required command-line arguments are missing");
        }

        String feedDomain = requireNonEmpty(args[0], "Feed domain key must be specified as first argument in command-line parameters");
        short bookmakerId = Short.parseShort(args[1]);

        Set<Short> sportIds = null;
        int nextIndex = 2;
        if (args.length > nextIndex && isPositionalSportIds(args[nextIndex])) {
            sportIds = parseShortSet(args[nextIndex]);
            nextIndex++;
        }

        Map<String, String> options = parseOptions(args, nextIndex);

        String literalApiKey = options.containsKey(API_KEY_OPTION)
                ? options.get(API_KEY_OPTION)
                : null;
        Path apiKeyFile = options.containsKey(API_KEY_FILE_OPTION)
                ? Paths.get(requireNonEmpty(options.get(API_KEY_FILE_OPTION), "apiKeyFile path must not be empty"))
                : null;
        String apiKey = ApiKeyResolver.resolve(literalApiKey, apiKeyFile);

        Path saveMessagesToFolder = options.containsKey(SAVE_MESSAGES_TO_FOLDER_OPTION)
                ? Paths.get(requireNonEmpty(options.get(SAVE_MESSAGES_TO_FOLDER_OPTION), "saveMessagesToFolder path must not be empty"))
                : Paths.get(DEFAULT_SAVE_MESSAGES_FOLDER);

        boolean groupMessagesByEvent = options.containsKey(GROUP_MESSAGES_BY_EVENT_OPTION)
                && parseBooleanOption(GROUP_MESSAGES_BY_EVENT_KEY, options.get(GROUP_MESSAGES_BY_EVENT_OPTION));

        Set<String> locales = options.containsKey(LOCALES_KEY)
                ? parseStringSet(options.get(LOCALES_KEY))
                : null;

        Short rawIdOriginBookmakerId = options.containsKey(RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION)
                ? Short.parseShort(requireNonEmpty(options.get(RAW_ID_ORIGIN_BOOKMAKER_ID_OPTION), "rawIdOriginBookmakerId must not be empty"))
                : null;

        Boolean fillRawOutcomeId = options.containsKey(FILL_RAW_OUTCOME_ID_OPTION)
                ? parseBooleanOption(FILL_RAW_OUTCOME_ID_KEY, options.get(FILL_RAW_OUTCOME_ID_OPTION))
                : null;

        Boolean fillDirectLink = options.containsKey(FILL_DIRECT_LINK_OPTION)
                ? parseBooleanOption(FILL_DIRECT_LINK_KEY, options.get(FILL_DIRECT_LINK_OPTION))
                : null;

        Set<Long> recordOnlyEventIds = options.containsKey(RECORD_ONLY_EVENT_IDS_OPTION)
                ? parseLongSet(options.get(RECORD_ONLY_EVENT_IDS_OPTION))
                : null;

        Set<String> recordOnlyRawEventIds = options.containsKey(RECORD_ONLY_RAW_EVENT_IDS_OPTION)
                ? parseStringSet(options.get(RECORD_ONLY_RAW_EVENT_IDS_OPTION))
                : null;

        return new TradingFeedReaderConfiguration(
                feedDomain,
                apiKey,
                bookmakerId,
                sportIds,
                saveMessagesToFolder,
                groupMessagesByEvent,
                locales,
                rawIdOriginBookmakerId,
                fillRawOutcomeId,
                fillDirectLink,
                recordOnlyEventIds,
                recordOnlyRawEventIds
        );
    }

    private Map<String, String> parseOptions(String[] args, int startIndex) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = startIndex; i < args.length; i++) {
            String argument = args[i];
            if (argument.startsWith(OPTION_PREFIX)) {
                argument = argument.substring(OPTION_PREFIX.length());
            }
            int separatorIndex = argument.indexOf('=');
            if (separatorIndex >= 0) {
                String key = argument.substring(0, separatorIndex).toLowerCase(Locale.ROOT);
                String value = argument.substring(separatorIndex + 1);
                options.put(key, value);
                continue;
            }
            String key = argument.toLowerCase(Locale.ROOT);
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for option: " + argument);
            }
            options.put(key, args[++i]);
        }
        return options;
    }

    private boolean isPositionalSportIds(String value) {
        return !value.startsWith(OPTION_PREFIX) && !value.contains("=") && value.matches(POSITIONAL_SPORT_IDS_REGEX);
    }

    private Set<Short> parseShortSet(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(Short::parseShort)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> parseLongSet(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> parseStringSet(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Boolean parseBooleanOption(String optionName, String value) {
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException(optionName + " must be either true or false");
    }

    private String requireNonEmpty(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
