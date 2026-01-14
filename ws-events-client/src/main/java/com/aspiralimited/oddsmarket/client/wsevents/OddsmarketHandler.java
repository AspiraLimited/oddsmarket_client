package com.aspiralimited.oddsmarket.client.wsevents;

import com.aspiralimited.oddsmarket.client.wsevents.dto.Event;
import com.aspiralimited.oddsmarket.client.wsevents.dto.EventLiveInfo;
import com.aspiralimited.oddsmarket.client.wsevents.dto.EventPlayer;
import com.aspiralimited.oddsmarket.client.wsevents.exception.MessageException;
import com.aspiralimited.oddsmarket.client.wsevents.message.EventDeletedMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.EventLiveInfoMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.EventMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.EventPlayersMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.FieldsMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.InitialStateTransferredMessage;
import com.aspiralimited.oddsmarket.client.wsevents.message.WebSocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class OddsmarketHandler {

    private final Listener listener = new Listener(this);
    private final List<OddsmarketEventsListener> eventsListeners = new CopyOnWriteArrayList<>();

    private final Map<Long, Event> eventMap = new ConcurrentHashMap<>();
    private final Map<Long, EventLiveInfo> eventLiveInfoMap = new ConcurrentHashMap<>();
    private final Map<Long, Map<Integer, EventPlayer>> eventPlayersMap = new ConcurrentHashMap<>();

    private List<String> eventFields;
    private List<String> eventLiveInfoFields;
    private List<String> eventPlayersFields;
    private List<String> eventPlayerFields;

    private final OddsmarketClient client;
    private final ObjectMapper objectMapper;

    public OddsmarketHandler(OddsmarketClient client) {
        this.client = client;
        this.client.addWebSocketListener(listener);
        this.objectMapper = client.getObjectMapper();
    }

    public void addEventsListener(OddsmarketEventsListener listener) {
        eventsListeners.add(listener);
    }

    public void removeEventsListener(OddsmarketEventsListener listener) {
        eventsListeners.remove(listener);
    }

    public Map<Long, Event> getEventMap() {
        return Collections.unmodifiableMap(eventMap);
    }

    public Map<Long, EventLiveInfo> getEventLiveInfoMap() {
        return Collections.unmodifiableMap(eventLiveInfoMap);
    }

    public Map<Long, Map<Integer, EventPlayer>> getEventPlayersMap() {
        return Collections.unmodifiableMap(eventPlayersMap);
    }

    private void onWebSocketMessage(WebSocketMessage message) {
        if (message instanceof InitialStateTransferredMessage) {
            onInitialStateTransferred();
        } else if (message instanceof FieldsMessage) {
            handleFieldsMessage((FieldsMessage) message);
        } else if (message instanceof EventMessage) {
            handleEventMessage((EventMessage) message);
        } else if (message instanceof EventLiveInfoMessage) {
            handleEventLiveInfoMessage((EventLiveInfoMessage) message);
        } else if (message instanceof EventDeletedMessage) {
            handleEventDeletedMessage((EventDeletedMessage) message);
        } else if (message instanceof EventPlayersMessage) {
            handleEventPlayersMessage((EventPlayersMessage) message);
        }
    }

    private void handleFieldsMessage(FieldsMessage message) {
        eventFields = message.getData().getEvent();
        eventLiveInfoFields = message.getData().getEventLiveInfo();
        eventPlayersFields = message.getData().getEventPlayers();
        eventPlayerFields = message.getData().getEventPlayer();
    }

    private void handleEventMessage(EventMessage message) {
        try {
            onEvent(parseObject(eventFields, message.getData(), Event.class));
        } catch (MessageException e) {
            log.error("Error parsing event", e);
        }
    }

    private void handleEventLiveInfoMessage(EventLiveInfoMessage message) {
        try {
            onEventLiveInfo(parseObject(eventLiveInfoFields, message.getData(), EventLiveInfo.class));
        } catch (MessageException e) {
            log.error("Error parsing event live info", e);
        }
    }

    private void handleEventDeletedMessage(EventDeletedMessage message) {
        onEventDeleted(message.getEventId());
    }

    private void handleEventPlayersMessage(EventPlayersMessage message) {
        try {
            RawEventPlayers rawEventPlayers = parseObject(eventPlayersFields, message.getData(), RawEventPlayers.class);
            List<EventPlayer> eventPlayers = new ArrayList<>();
            for (JsonNode playerData : rawEventPlayers.getPlayers()) {
                eventPlayers.add(parseObject(eventPlayerFields, playerData, EventPlayer.class));
            }
            onEventPlayers(rawEventPlayers.getEventId(), eventPlayers, rawEventPlayers.getRemovedPlayers());
        } catch (MessageException e) {
            log.error("Error parsing event players", e);
        }
    }

    private <T> T parseObject(List<String> fields, JsonNode data, Class<T> clazz) throws MessageException {
        if (fields == null) {
            throw new MessageException("Fields not received");
        }

        if (!data.isArray() || data.size() != fields.size()) {
            throw new MessageException("Invalid fields count");
        }

        ObjectNode objectNode = objectMapper.createObjectNode();

        for (int i = 0; i < fields.size(); i++) {
            objectNode.set(fields.get(i), data.get(i));
        }

        try {
            return objectMapper.treeToValue(objectNode, clazz);
        } catch (JsonProcessingException e) {
            throw new MessageException("Error parsing object", e);
        }
    }

    private void onInitialStateTransferred() {
        for (OddsmarketEventsListener eventsListener : eventsListeners) {
            eventsListener.onInitialStateTransferred();
        }
    }

    private void onEvent(Event event) {
        eventMap.put(event.getId(), event);
        for (OddsmarketEventsListener eventsListener : eventsListeners) {
            eventsListener.onEvent(event);
        }
    }

    private void onEventLiveInfo(EventLiveInfo eventLiveInfo) {
        eventLiveInfoMap.put(eventLiveInfo.getEventId(), eventLiveInfo);
        for (OddsmarketEventsListener eventsListener : eventsListeners) {
            eventsListener.onEventLiveInfo(eventLiveInfo);
        }
    }

    private void onEventDeleted(long eventId) {
        Event event = eventMap.remove(eventId);
        eventLiveInfoMap.remove(eventId);
        eventPlayersMap.remove(eventId);
        if (event != null) {
            for (OddsmarketEventsListener eventsListener : eventsListeners) {
                eventsListener.onEventDeleted(event);
            }
        } else {
            log.warn("Event not found: {}", eventId);
        }
    }

    private void onEventPlayers(long eventId, List<EventPlayer> players, Set<Integer> removedPlayers) {
        if (!eventMap.containsKey(eventId)) {
            return;
        }

        Map<Integer, EventPlayer> playerMap = eventPlayersMap.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());
        for (EventPlayer player : players) {
            playerMap.put(player.getId(), player);
        }
        playerMap.keySet().removeAll(removedPlayers);

        for (OddsmarketEventsListener eventsListener : eventsListeners) {
            eventsListener.onEventPlayers(eventId, players, removedPlayers);
        }
    }

    public void shutdown() {
        this.client.removeWebSocketListener(listener);
    }

    @RequiredArgsConstructor
    private static class Listener implements OddsmarketWebSocketListener {

        private final OddsmarketHandler handler;

        @Override
        public void onWebSocketMessage(WebSocketMessage message) {
            handler.onWebSocketMessage(message);
        }
    }

    @Value
    @Builder
    @Jacksonized
    private static class RawEventPlayers {

        long eventId;
        List<JsonNode> players;
        Set<Integer> removedPlayers;
    }
}
