package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class InteractiveTradingFeedReaderPrompter {

    public TradingFeedReaderConfiguration prompt() {
        Scanner scanner = new Scanner(System.in);

        String feedDomain = promptRequired(scanner, "Trading feed domain (for example api-pr.oddsmarket.org): ");
        String apiKey = promptRequired(scanner, "API key: ");
        short bookmakerId = Short.parseShort(promptRequired(scanner, "Trading Feed ID: "));

        Set<Short> sportIds = parseShortSet(promptOptional(scanner, "Sport IDs (comma-separated, optional): "));

        boolean saveMessages = Boolean.parseBoolean(promptBoolean(scanner, "Save all incoming messages to folder? (y/n): "));
        Path saveMessagesToFolder = null;
        if (saveMessages) {
            saveMessagesToFolder = Paths.get(promptRequired(scanner, "Folder path for saveMessagesToFolder: "));
        }
        boolean groupMessagesByEvent = Boolean.parseBoolean(promptBoolean(scanner, "Group saved message files by event ID in the filename? (y/n): "));

        Set<String> locales = parseStringSet(promptOptional(scanner, "Locales (comma-separated ISO codes, optional): "));
        Short rawIdOriginBookmakerId = parseShort(promptOptional(scanner, "rawIdOriginBookmakerId (optional): "));
        Boolean fillRawOutcomeId = parseBoolean(promptOptional(scanner, "fillRawOutcomeId (true/false, optional): "));
        Boolean fillDirectLink = parseBoolean(promptOptional(scanner, "fillDirectLink (true/false, optional): "));

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
                fillDirectLink
        );
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

    private String promptBoolean(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
                return "true";
            }
            if ("n".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
                return "false";
            }
            System.out.println("Please answer y or n.");
        }
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

    private Short parseShort(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Short.parseShort(value);
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new IllegalArgumentException("Boolean value must be true or false");
    }
}
