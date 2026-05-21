package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Scanner;
import java.util.Set;

import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.DEFAULT_API_KEY_FILE;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.DEFAULT_SAVE_MESSAGES_FOLDER;
import static com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.INTERACTIVE_PASTE_API_KEY_VALUE;

public class InteractiveTradingFeedReaderPrompter {
    private static final String LIVE_DOMAIN_ALIAS = "live";
    private static final String PREMATCH_DOMAIN_ALIAS = "prematch";
    private static final String LIVE_DOMAIN = "api-lv.oddsmarket.org";
    private static final String PREMATCH_DOMAIN = "api-pr.oddsmarket.org";

    public TradingFeedReaderConfiguration prompt() {
        Scanner scanner = new Scanner(System.in);

        String feedDomain = normalizeDomain(promptRequired(scanner,
                "oddsmarket domain (enter 'live' or 'prematch' or enter full domain, like api-pr.oddsmarket.org): "));
        String apiKey = promptApiKey(scanner);
        short bookmakerId = Short.parseShort(promptRequired(scanner, "Trading Feed ID: "));

        Set<Short> sportIds = CliValueParsers.parseShortSet(promptOptional(scanner,
                "[optional](default - all sports) Sport IDs (comma-separated): "));

        Path saveMessagesToFolder = Paths.get(promptOptionalWithDefault(scanner,
                "[optional](default - './" + DEFAULT_SAVE_MESSAGES_FOLDER + "') Folder path for saveMessagesToFolder: ",
                DEFAULT_SAVE_MESSAGES_FOLDER));
        Set<Long> recordOnlyEventIds = CliValueParsers.parseLongSet(promptOptional(scanner,
                "[optional](default - record all) Record only specific OddsMarket event IDs (comma-separated): "));
        Set<String> recordOnlyRawEventIds = CliValueParsers.parseStringSet(promptOptional(scanner,
                "[optional](default - record all) Record only specific raw bookmaker event IDs (comma-separated, requires rawIdOriginBookmakerId): "));
        boolean groupMessagesByEvent = promptBooleanWithDefault(scanner,
                "[optional](default - no) Group saved message files by event ID in the filename? (y/n): ", false);

        Set<String> locales = CliValueParsers.parseStringSet(promptOptional(scanner,
                "[optional](default - en locale) Locales (comma-separated ISO codes): "));
        Short rawIdOriginBookmakerId = CliValueParsers.parseShort(promptOptional(scanner,
                "[optional](default - not specified) rawIdOriginBookmakerId: "));
        Boolean fillRawOutcomeId = CliValueParsers.parseOptionalBoolean(promptOptional(scanner,
                "[optional](default - false) fillRawOutcomeId (true/false): "));
        Boolean fillDirectLink = CliValueParsers.parseOptionalBoolean(promptOptional(scanner,
                "[optional](default - false) fillDirectLink (true/false): "));

        Duration duration = CliValueParsers.parseDuration(promptOptional(scanner,
                "[optional](default - run until Ctrl+C) Maximum session duration (e.g. 30s, 5m, 1h): "));
        Long maxMessages = CliValueParsers.parsePositiveLong("maxMessages", promptOptional(scanner,
                "[optional](default - unlimited) Maximum messages recorded to disk: "));

        return TradingFeedReaderConfiguration.builder()
                .feedDomain(feedDomain)
                .apiKey(apiKey)
                .tradingFeedId(bookmakerId)
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

    private String promptApiKey(Scanner scanner) {
        while (true) {
            String response = promptOptional(scanner,
                    "API key file path [Enter for default '" + DEFAULT_API_KEY_FILE
                            + "', or '" + INTERACTIVE_PASTE_API_KEY_VALUE + "' to type the key directly]: ");
            if (INTERACTIVE_PASTE_API_KEY_VALUE.equalsIgnoreCase(response)) {
                String literal = promptRequired(scanner, "API key: ");
                return ApiKeyResolver.resolve(literal, null);
            }
            Path apiKeyFile = response.isEmpty() ? null : Paths.get(response);
            try {
                return ApiKeyResolver.resolve(null, apiKeyFile);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private String promptRequired(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This value is required.");
        }
    }

    private String promptOptional(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String promptOptionalWithDefault(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private boolean promptBooleanWithDefault(Scanner scanner, String prompt, boolean defaultValue) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return defaultValue;
            }
            if ("y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
                return true;
            }
            if ("n".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
                return false;
            }
            System.out.println("Please answer y or n.");
        }
    }

    private String normalizeDomain(String value) {
        if (LIVE_DOMAIN_ALIAS.equalsIgnoreCase(value)) {
            return LIVE_DOMAIN;
        }
        if (PREMATCH_DOMAIN_ALIAS.equalsIgnoreCase(value)) {
            return PREMATCH_DOMAIN;
        }
        return value;
    }
}
