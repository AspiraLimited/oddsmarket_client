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

public class TradingFeedReaderCliParser {

    public boolean isInteractiveMode(String[] args) {
        return args.length == 1 && ("--interactive".equalsIgnoreCase(args[0]) || "interactive".equalsIgnoreCase(args[0]));
    }

    public TradingFeedReaderConfiguration parse(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Required command-line arguments are missing");
        }

        String feedDomain = requireNonEmpty(args[0], "Feed domain key must be specified as first argument in command-line parameters");
        String apiKey = requireNonEmpty(args[1], "API key must be specified in command-line parameters");
        short bookmakerId = Short.parseShort(args[2]);

        Set<Short> sportIds = null;
        int nextIndex = 3;
        if (args.length > nextIndex && isPositionalSportIds(args[nextIndex])) {
            sportIds = parseShortSet(args[nextIndex]);
            nextIndex++;
        }

        Map<String, String> options = parseOptions(args, nextIndex);

        Path saveMessagesToFolder = options.containsKey("savemessagestofolder")
                ? Paths.get(requireNonEmpty(options.get("savemessagestofolder"), "saveMessagesToFolder path must not be empty"))
                : null;

        Set<String> locales = options.containsKey("locales")
                ? parseStringSet(options.get("locales"))
                : null;

        Short rawIdOriginBookmakerId = options.containsKey("rawidoriginbookmakerid")
                ? Short.parseShort(requireNonEmpty(options.get("rawidoriginbookmakerid"), "rawIdOriginBookmakerId must not be empty"))
                : null;

        Boolean fillRawOutcomeId = options.containsKey("fillrawoutcomeid")
                ? parseBooleanOption("fillRawOutcomeId", options.get("fillrawoutcomeid"))
                : null;

        Boolean fillDirectLink = options.containsKey("filldirectlink")
                ? parseBooleanOption("fillDirectLink", options.get("filldirectlink"))
                : null;

        return new TradingFeedReaderConfiguration(
                feedDomain,
                apiKey,
                bookmakerId,
                sportIds,
                saveMessagesToFolder,
                locales,
                rawIdOriginBookmakerId,
                fillRawOutcomeId,
                fillDirectLink
        );
    }

    private Map<String, String> parseOptions(String[] args, int startIndex) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = startIndex; i < args.length; i++) {
            String argument = args[i];
            if (argument.startsWith("--")) {
                argument = argument.substring(2);
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
        return !value.startsWith("--") && !value.contains("=") && value.matches("^\\d+(,\\d+)*$");
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
