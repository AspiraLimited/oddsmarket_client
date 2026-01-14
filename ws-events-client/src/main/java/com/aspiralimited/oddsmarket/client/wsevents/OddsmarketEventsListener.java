package com.aspiralimited.oddsmarket.client.wsevents;

import com.aspiralimited.oddsmarket.client.wsevents.dto.Event;
import com.aspiralimited.oddsmarket.client.wsevents.dto.EventLiveInfo;
import com.aspiralimited.oddsmarket.client.wsevents.dto.EventPlayer;

import java.util.List;
import java.util.Set;

public interface OddsmarketEventsListener {

    void onInitialStateTransferred();

    void onEvent(Event event);

    void onEventDeleted(Event event);

    void onEventLiveInfo(EventLiveInfo eventLiveInfo);

    void onEventPlayers(long eventId, List<EventPlayer> players, Set<Integer> removedPlayers);
}
