package com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl;

import com.aspiralimited.oddsmarket.api.v4.websocket.trading.dto.OddsmarketTradingDto;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.TradingFeedListener;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.listener.impl.model.InMemoryStateStorage;
import com.aspiralimited.oddsmarket.client.tradingfeed.websocket.model.TradingFeedConnectionStatusCode;
import lombok.Getter;

public class TradingFeedStateKeepingListener implements TradingFeedListener {
    @Getter
    protected InMemoryStateStorage inMemoryStateStorage = new InMemoryStateStorage();
    @Getter
    private volatile TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode;

    @Override
    public void onServerMessage(OddsmarketTradingDto.ServerMessage serverMessage) {
        switch (serverMessage.getPayloadCase()) {
            case EVENTSNAPSHOT:
                OddsmarketTradingDto.EventSnapshot eventSnapshot = serverMessage.getEventSnapshot();
                inMemoryStateStorage.addEvent(eventSnapshot);
                break;
            case EVENTPATCH:
                OddsmarketTradingDto.EventPatch eventPatch = serverMessage.getEventPatch();
                inMemoryStateStorage.updateEvent(eventPatch);
                break;
            case EVENTSREMOVED:
                OddsmarketTradingDto.EventsRemoved eventsRemoved = serverMessage.getEventsRemoved();
                inMemoryStateStorage.removeEvent(eventsRemoved);
                break;
            case INITIALSYNCCOMPLETE:
                OddsmarketTradingDto.InitialSyncComplete initialSyncComplete = serverMessage.getInitialSyncComplete();

                break;
            case HEARTBEAT:
                break;
            case ERRORMESSAGE:
                OddsmarketTradingDto.ErrorMessage errorMessage = serverMessage.getErrorMessage();
                break;
            case PAYLOAD_NOT_SET:
                break;
        }
    }

    @Override
    public void onConnectError(TradingFeedConnectionStatusCode tradingFeedConnectionStatusCode) {
        this.tradingFeedConnectionStatusCode = tradingFeedConnectionStatusCode;
    }


    public void clearStorage() {
        inMemoryStateStorage.clearAll();
    }
}
