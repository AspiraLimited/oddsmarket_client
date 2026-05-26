package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class CliValueParsers {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)(s|m|h)$");

    private CliValueParsers() {
    }

    static Set<Short> parseShortSet(String value) {
        if (isBlank(value)) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(Short::parseShort)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static Set<Long> parseLongSet(String value) {
        if (isBlank(value)) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static Set<String> parseStringSet(String value) {
        if (isBlank(value)) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static Short parseShort(String value) {
        if (isBlank(value)) {
            return null;
        }
        return Short.parseShort(value);
    }

    static Long parsePositiveLong(String optionName, String value) {
        if (isBlank(value)) {
            return null;
        }
        long parsed = Long.parseLong(value.trim());
        if (parsed <= 0) {
            throw new IllegalArgumentException(optionName + " must be a positive number, got: " + parsed);
        }
        return parsed;
    }

    /**
     * Parses a duration with a single-unit suffix: {@code 30s}, {@code 5m}, {@code 1h}.
     * Returns {@code null} for blank input.
     */
    static Duration parseDuration(String value) {
        if (isBlank(value)) {
            return null;
        }
        Matcher matcher = DURATION_PATTERN.matcher(value.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid duration: '" + value + "'. Expected format: <number><s|m|h>, e.g. 30s, 5m, 1h"
            );
        }
        long amount = Long.parseLong(matcher.group(1));
        switch (matcher.group(2)) {
            case "s": return Duration.ofSeconds(amount);
            case "m": return Duration.ofMinutes(amount);
            case "h": return Duration.ofHours(amount);
            default: throw new IllegalStateException("Unreachable duration unit: " + matcher.group(2));
        }
    }

    static Boolean parseOptionalBoolean(String value) {
        if (isBlank(value)) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new IllegalArgumentException("Boolean value must be true or false");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
