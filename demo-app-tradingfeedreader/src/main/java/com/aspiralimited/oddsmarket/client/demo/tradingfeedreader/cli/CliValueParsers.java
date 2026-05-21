package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class CliValueParsers {

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

    static Boolean parseOptionalBoolean(String value) {
        if (isBlank(value)) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new IllegalArgumentException("Boolean value must be true or false");
    }

    static boolean parseRequiredBoolean(String optionName, String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException(optionName + " must be either true or false");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
