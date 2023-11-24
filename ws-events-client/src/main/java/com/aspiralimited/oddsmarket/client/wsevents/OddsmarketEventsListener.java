package com.aspiralimited.oddsmarket.client.wsevents;

import com.aspiralimited.oddsmarket.client.wsevents.dto.Event;
import com.aspiralimited.oddsmarket.client.wsevents.dto.EventLiveInfo;

public interface OddsmarketEventsListener {

    void onEvent(Event event);

    void onEventDeleted(Event event);

    void onEventLiveInfo(EventLiveInfo eventLiveInfo);
}
