package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum MatchStatus {
    UNKNOWN((short) 0),
    NOT_STARTED((short) 1),
    POSTPONED((short) 3),
    CANCELLED((short) 5),
    LIVE((short) 10),
    PAUSED((short) 11),
    BREAK((short) 12),
    SUSPENDED((short) 13),
    INTERRUPTED((short) 15),
    FINISHED((short) 20);

    private static final Map<Short, MatchStatus> byId = new HashMap<>();

    static {
        for (MatchStatus value : values()) {
            byId.put(value.id, value);
        }
    }

    private final short id;

    public static MatchStatus byId(short id) {
        return byId.get(id);
    }
}
