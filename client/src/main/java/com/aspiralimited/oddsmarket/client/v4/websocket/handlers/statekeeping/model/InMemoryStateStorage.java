package com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.dto.BookmakerEventDto;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStateStorage {

    @Getter
    Map<Long, BookmakerEventState> bookmakerEventById = new ConcurrentHashMap<>();

    public boolean hasBookmakerEvent(long bookmakerEventId) {
        return bookmakerEventById.containsKey(bookmakerEventId);
    }

    public BookmakerEventState getBookmakerEvent(long bookmakerEventId) {
        return bookmakerEventById.get(bookmakerEventId);
    }

    public BookmakerEventState putBookmakerEvent(BookmakerEventDto bkEvent) {
        BookmakerEventState newBkEventstate = new BookmakerEventState(bkEvent.id);
        newBkEventstate.updateProperties(bkEvent);
        return bookmakerEventById.put(bkEvent.id, newBkEventstate);
    }

    public BookmakerEventState removeBookmakerEvent(long bookmakerEventId) {
        return bookmakerEventById.remove(bookmakerEventId);
    }
}
