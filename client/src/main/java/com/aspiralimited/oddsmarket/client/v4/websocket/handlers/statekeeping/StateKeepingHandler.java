package com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping;

import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.Handler;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model.BookmakerEventState;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model.InMemoryStateStorage;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model.OutcomeKey;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.BookmakerEventDto;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

public abstract class StateKeepingHandler extends Handler {

    @Getter
    protected final InMemoryStateStorage inMemoryStateStorage = new InMemoryStateStorage();

    @Override
    protected void internalInfo(String msg) {
        super.internalInfo(msg);
    }

    @Override
    protected void internalBookmakerEvent(BookmakerEventDto bkEvent) {
        long bookmakerEventId = bkEvent.id;
        if (inMemoryStateStorage.hasBookmakerEvent(bookmakerEventId)) {
            BookmakerEventState bookmakerEventState = inMemoryStateStorage.getBookmakerEvent(bookmakerEventId);
            bookmakerEventState.updateProperties(bkEvent);
        } else {
            BookmakerEventState bookmakerEventState = inMemoryStateStorage.putBookmakerEvent(bkEvent);
        }

        super.internalBookmakerEvent(bkEvent);
    }

    @Override
    protected void internalOutcomes(List<OutcomeDto> updatedOutcomeDtos) {
        for (OutcomeDto outcomeDto : updatedOutcomeDtos) {
            long bookmakerEventId = outcomeDto.bookmakerEventId;
            if (inMemoryStateStorage.hasBookmakerEvent(bookmakerEventId)) {
                BookmakerEventState bookmakerEventState = inMemoryStateStorage.getBookmakerEvent(bookmakerEventId);
                OutcomeKey outcomeKey = new OutcomeKey(outcomeDto.marketAndBetTypeId,
                        outcomeDto.marketAndBetTypeParameterValue,
                        (short) (int) outcomeDto.periodIdentifier,
                        outcomeDto.isLay,
                        outcomeDto.playerId1,
                        outcomeDto.playerId2
                );
                if (bookmakerEventState.hasOutcome(outcomeKey)) {
                    if (outcomeDto.active()) {
                        bookmakerEventState.putOutcome(outcomeKey, outcomeDto);
                    } else {
                        // Deactivated outcomes must be removed
                        bookmakerEventState.removeOutcome(outcomeKey);
                    }
                } else {
                    if (outcomeDto.active()) {
                        bookmakerEventState.putOutcome(outcomeKey, outcomeDto);
                    } else {
                        throw new IllegalStateException("Missing outcome is being deactivated. ID=" + outcomeDto.id);
                    }
                }
            } else {
                throw new IllegalStateException("Missing bookmaker event information for id=" + bookmakerEventId);
            }

        }

        super.internalOutcomes(updatedOutcomeDtos);
    }

    @Override
    protected void internalRemoveBookmakerEvents(Collection<Long> bookmakerEventIds) {
        for (Long bookmakerEventId : bookmakerEventIds) {
            BookmakerEventState bkEvent = inMemoryStateStorage.removeBookmakerEvent(bookmakerEventId);
            if (bkEvent == null) {
                throw new IllegalStateException("Missing bookmaker event information when trying to remove bookmaker event id=" + bookmakerEventId);
            }
        }

        super.internalRemoveBookmakerEvents(bookmakerEventIds);
    }

}
