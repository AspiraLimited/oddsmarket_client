package com.aspiralimited.oddsmarket.client.websocket.handlers;

import com.aspiralimited.oddsmarket.api.v4.websocket.dto.BookmakerEventDto;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class Handler {
    private final List<String> outcomeFields = new ArrayList<>();
    private final List<String> bookmakerEventFields = new ArrayList<>();

    public void handle(JSONObject jsonMsg) {
        String command = jsonMsg.optString("cmd");

        switch (command) {
            case "fields":
                outcomeFields.clear();
                outcomeFields.addAll( jsonMsg.getJSONObject("msg").getJSONArray("Outcome").toList().stream().map(x -> (String) x).collect(toList()));
                bookmakerEventFields.clear();
                bookmakerEventFields.addAll(jsonMsg.getJSONObject("msg").getJSONArray("BookmakerEvent").toList().stream().map(x -> (String) x).collect(toList()));
                this.internalInfo("init fields: " + jsonMsg);
                break;

            case "bookmaker_events":
                for (Object raw : jsonMsg.getJSONArray("msg")) {
                    BookmakerEventDto bkEvent = new BookmakerEventDto(((JSONArray) raw).toList(), bookmakerEventFields);
                    internalBookmakerEvent(bkEvent);
                }
                break;

            case "outcomes":
                List<OutcomeDto> updatedOdds = new ArrayList<>();

                for (Object raw : jsonMsg.getJSONArray("msg")) {
                    OutcomeDto outcomeDto = new OutcomeDto(((JSONArray) raw).toList(), outcomeFields);
                    updatedOdds.add(outcomeDto);
                }

                internalOutcomes(updatedOdds);

                break;

            case "bookmaker_events_removed":
                List<Long> ids = new ArrayList<>();
                jsonMsg.getJSONObject("msg").getJSONArray("bookmakerEventIds").forEach(raw -> {
                    if (raw instanceof Integer) {
                        ids.add((long) (int) raw);
                    } else if (raw instanceof Long) {
                        ids.add((long) raw);
                    }
                });

                internalRemoveBookmakerEvents(ids);
                break;

            case "subscribed":
                internalInfo("Subscribed successfully: " + jsonMsg);
                break;

            case "error":
                internalInfo("Error message: " + jsonMsg.get("msg"));
                break;

            default:
                internalInfo("skip command '" + command + "' with msg '" + jsonMsg.get("msg") + "'");
        }
    }

    protected void internalInfo(String msg) {
        info(msg);
    }

    protected void internalBookmakerEvent(BookmakerEventDto bkEvent) {
        bookmakerEvent(bkEvent);
    }

    protected void internalOutcomes(List<OutcomeDto> updatedOutcomeDtos) {
        outcomes(updatedOutcomeDtos);
    }

    protected void internalRemoveBookmakerEvents(Collection<Long> ids) {
        removeBookmakerEvents(ids);
    }


    public abstract void info(String msg);

    public abstract void bookmakerEvent(BookmakerEventDto bkEvent);

    public abstract void outcomes(List<OutcomeDto> updatedOutcomeDtos);

    public abstract void removeBookmakerEvents(Collection<Long> ids);

    public abstract void onDisconnected(boolean closedByServer);
}
