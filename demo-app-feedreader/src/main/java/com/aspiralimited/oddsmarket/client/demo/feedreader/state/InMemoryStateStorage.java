package com.aspiralimited.oddsmarket.client.demo.feedreader.state;

import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStateStorage {

    Map<Long, BookmakerEventState> bookmakerEventById = new ConcurrentHashMap<>();

    public boolean hasBookmakerEvent(long bookmakerEventId) {
        return bookmakerEventById.containsKey(bookmakerEventId);
    }

    public BookmakerEventState getBookmakerEvent(long bookmakerEventId) {
        return bookmakerEventById.get(bookmakerEventId);
    }

    public BookmakerEventState putBookmakerEvent(BookmakerEvent bkEvent) {
        BookmakerEventState newBkEventstate = new BookmakerEventState(bkEvent.id);
        newBkEventstate.updatePropertiesAndReturnDiff(bkEvent);
        return bookmakerEventById.put(bkEvent.id, newBkEventstate);
    }

    public BookmakerEventState removeBookmakerEvent(long bookmakerEventId) {
        return bookmakerEventById.remove(bookmakerEventId);
    }
}
