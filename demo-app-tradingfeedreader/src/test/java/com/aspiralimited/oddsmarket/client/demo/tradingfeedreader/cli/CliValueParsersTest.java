package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliValueParsersTest {

    // ---- parseShortSet ----

    @Test
    void parseShortSet_blankInput_returnsNull() {
        assertNull(CliValueParsers.parseShortSet(null));
        assertNull(CliValueParsers.parseShortSet(""));
        assertNull(CliValueParsers.parseShortSet("   "));
    }

    @Test
    void parseShortSet_parsesCsvAndPreservesOrder() {
        Set<Short> result = CliValueParsers.parseShortSet("1,2,3");
        assertIterableEquals(linkedSet((short) 1, (short) 2, (short) 3), result);
    }

    @Test
    void parseShortSet_trimsWhitespaceAndDropsEmpty() {
        Set<Short> result = CliValueParsers.parseShortSet(" 1 ,2,,3 ");
        assertIterableEquals(linkedSet((short) 1, (short) 2, (short) 3), result);
    }

    @Test
    void parseShortSet_invalidNumber_throws() {
        assertThrows(NumberFormatException.class, () -> CliValueParsers.parseShortSet("1,abc"));
    }

    // ---- parseLongSet ----

    @Test
    void parseLongSet_blankInput_returnsNull() {
        assertNull(CliValueParsers.parseLongSet(null));
        assertNull(CliValueParsers.parseLongSet(""));
    }

    @Test
    void parseLongSet_parsesLargeNumbers() {
        Set<Long> result = CliValueParsers.parseLongSet("9999999999,1");
        assertIterableEquals(linkedSet(9999999999L, 1L), result);
    }

    @Test
    void parseLongSet_invalidNumber_throws() {
        assertThrows(NumberFormatException.class, () -> CliValueParsers.parseLongSet("abc"));
    }

    // ---- parseStringSet ----

    @Test
    void parseStringSet_blankInput_returnsNull() {
        assertNull(CliValueParsers.parseStringSet(null));
        assertNull(CliValueParsers.parseStringSet("   "));
    }

    @Test
    void parseStringSet_trimsAndDropsEmpty() {
        Set<String> result = CliValueParsers.parseStringSet(" en , ru ,, de ");
        assertIterableEquals(linkedSet("en", "ru", "de"), result);
    }

    // ---- parseShort ----

    @Test
    void parseShort_blankInput_returnsNull() {
        assertNull(CliValueParsers.parseShort(null));
        assertNull(CliValueParsers.parseShort(""));
        assertNull(CliValueParsers.parseShort("  "));
    }

    @Test
    void parseShort_parsesValue() {
        assertEquals(Short.valueOf((short) 42), CliValueParsers.parseShort("42"));
    }

    @Test
    void parseShort_invalidNumber_throws() {
        assertThrows(NumberFormatException.class, () -> CliValueParsers.parseShort("abc"));
    }

    // ---- parsePositiveLong ----

    @Test
    void parsePositiveLong_blankInput_returnsNull() {
        assertNull(CliValueParsers.parsePositiveLong("--maxMessages", null));
        assertNull(CliValueParsers.parsePositiveLong("--maxMessages", ""));
    }

    @Test
    void parsePositiveLong_parsesPositive() {
        assertEquals(Long.valueOf(1000L), CliValueParsers.parsePositiveLong("--maxMessages", "1000"));
    }

    @Test
    void parsePositiveLong_trimsWhitespace() {
        assertEquals(Long.valueOf(7L), CliValueParsers.parsePositiveLong("--maxMessages", "  7  "));
    }

    @Test
    void parsePositiveLong_zero_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CliValueParsers.parsePositiveLong("--maxMessages", "0"));
        assertTrue(ex.getMessage().contains("--maxMessages"), () -> "Message should mention option name; got: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("positive"), () -> "Message should mention 'positive'; got: " + ex.getMessage());
    }

    @Test
    void parsePositiveLong_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> CliValueParsers.parsePositiveLong("--maxMessages", "-5"));
    }

    @Test
    void parsePositiveLong_invalidNumber_throws() {
        assertThrows(NumberFormatException.class,
                () -> CliValueParsers.parsePositiveLong("--maxMessages", "abc"));
    }

    // ---- parseDuration ----

    @Test
    void parseDuration_blankInput_returnsNull() {
        assertNull(CliValueParsers.parseDuration(null));
        assertNull(CliValueParsers.parseDuration("  "));
    }

    @Test
    void parseDuration_seconds() {
        assertEquals(Duration.ofSeconds(30), CliValueParsers.parseDuration("30s"));
    }

    @Test
    void parseDuration_minutes() {
        assertEquals(Duration.ofMinutes(5), CliValueParsers.parseDuration("5m"));
    }

    @Test
    void parseDuration_hours() {
        assertEquals(Duration.ofHours(1), CliValueParsers.parseDuration("1h"));
    }

    @Test
    void parseDuration_isCaseInsensitive() {
        assertEquals(Duration.ofMinutes(2), CliValueParsers.parseDuration("2M"));
    }

    @Test
    void parseDuration_trimsWhitespace() {
        assertEquals(Duration.ofSeconds(45), CliValueParsers.parseDuration("  45s  "));
    }

    @Test
    void parseDuration_missingUnit_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CliValueParsers.parseDuration("30"));
        assertTrue(ex.getMessage().contains("<s|m|h>"), () -> "Message should hint at expected format; got: " + ex.getMessage());
    }

    @Test
    void parseDuration_unsupportedUnit_throws() {
        assertThrows(IllegalArgumentException.class, () -> CliValueParsers.parseDuration("1d"));
        assertThrows(IllegalArgumentException.class, () -> CliValueParsers.parseDuration("100ms"));
    }

    @Test
    void parseDuration_garbage_throws() {
        assertThrows(IllegalArgumentException.class, () -> CliValueParsers.parseDuration("abc"));
    }

    // ---- parseOptionalBoolean ----

    @Test
    void parseOptionalBoolean_blankInput_returnsNull() {
        assertNull(CliValueParsers.parseOptionalBoolean(null));
        assertNull(CliValueParsers.parseOptionalBoolean(""));
        assertNull(CliValueParsers.parseOptionalBoolean("   "));
    }

    @Test
    void parseOptionalBoolean_trueAndFalse() {
        assertEquals(Boolean.TRUE, CliValueParsers.parseOptionalBoolean("true"));
        assertEquals(Boolean.FALSE, CliValueParsers.parseOptionalBoolean("false"));
    }

    @Test
    void parseOptionalBoolean_isCaseInsensitive() {
        assertEquals(Boolean.TRUE, CliValueParsers.parseOptionalBoolean("TRUE"));
        assertEquals(Boolean.FALSE, CliValueParsers.parseOptionalBoolean("False"));
    }

    @Test
    void parseOptionalBoolean_garbage_throws() {
        assertThrows(IllegalArgumentException.class, () -> CliValueParsers.parseOptionalBoolean("yes"));
        assertThrows(IllegalArgumentException.class, () -> CliValueParsers.parseOptionalBoolean("1"));
    }

    // ---- helpers ----

    @SafeVarargs
    private static <T> Set<T> linkedSet(T... values) {
        Set<T> set = new LinkedHashSet<>();
        for (T value : values) {
            set.add(value);
        }
        return set;
    }
}
