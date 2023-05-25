package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class ValueReader {

    private ValueReader() {
    }

    static float asFloat(Object value) {
        assertNotNull(value);

        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).floatValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else if (value instanceof Long) {
            return ((Long) value).floatValue();
        } else {
            return Float.parseFloat(value.toString());
        }
    }

    static short asShort(Object value) {
        assertNotNull(value);

        if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).shortValue();
        } else if (value instanceof Long) {
            return ((Long) value).shortValue();
        } else {
            return Short.parseShort(value.toString());
        }
    }

    static int asInt(Object value) {
        assertNotNull(value);

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else {
            return Integer.parseInt(value.toString());
        }
    }

    static long asLong(Object value) {
        assertNotNull(value);

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else {
            return Long.parseLong(value.toString());
        }
    }

    static boolean asBoolean(Object value) {
        assertNotNull(value);

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }

    static List<Object> asList(Object value) {
        assertNotNull(value);

        if (value instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) value;
            List<Object> list = new ArrayList<>();
            for (Object object : iterable) {
                list.add(object);
            }
            return list;
        } else {
            throw new IllegalArgumentException("Value is not iterable");
        }
    }

    private static void assertNotNull(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }
}
