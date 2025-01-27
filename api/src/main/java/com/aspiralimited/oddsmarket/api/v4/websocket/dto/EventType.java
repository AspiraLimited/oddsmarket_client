package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum EventType {
    REGULAR((short) 1),
    RACE((short) 2),
    FIELD((short) 3),
    OUTRIGHT((short) 4);

    private static final Map<Short, EventType> byId = new HashMap<>();

    static {
        for (EventType value : values()) {
            byId.put(value.id, value);
        }
    }

    private final short id;

    public static EventType byId(short id) {
        return byId.getOrDefault(id, null);
    }
}
