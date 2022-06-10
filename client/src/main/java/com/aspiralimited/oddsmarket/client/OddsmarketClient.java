package com.aspiralimited.oddsmarket.client;

import com.aspiralimited.oddsmarket.api.websocket.cmd.RequestCMD;
import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;
import com.aspiralimited.oddsmarket.client.models.Odd;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class OddsmarketClient {

    private final WebSocket ws;

    private Handler handler;

    private Consumer<String> onTextMessageConsumer;
    private Consumer<JSONObject> onJsonMessageConsumer;

    // TODO timeouts
    private OddsmarketClient(String host) throws IOException {
        this.ws = new WebSocketFactory().createSocket(host);

        ws.addListener(new WebSocketAdapter() {
            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                handler.onDisconnected(closedByServer);
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            }

            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                if (onTextMessageConsumer != null) onTextMessageConsumer.accept(message);

                JSONObject json = new JSONObject(message);
                if (onJsonMessageConsumer != null) onJsonMessageConsumer.accept(json);

                if (handler != null) handler.handle(json);
            }
        });
    }

    public static OddsmarketClient connect(String host) throws IOException, WebSocketException {
        return connect(host, null);
    }

    public static OddsmarketClient connect(String host, String apiKey) throws IOException, WebSocketException {
        OddsmarketClient client = new OddsmarketClient(host);
        client.connect();
        if (apiKey != null) client.auth(apiKey);
        return client;
    }

    public OddsmarketClient connect() throws WebSocketException {
        ws.connect();
        return this;
    }

    public OddsmarketClient disconnect() {
        ws.sendClose();
        ws.disconnect();
        return this;
    }

    public OddsmarketClient reconnect() throws IOException, WebSocketException {
        disconnect();
        ws.recreate().connect();
        return this;
    }

    // Listeners
    public void handler(Handler handler) {
        this.handler = handler;
    }

    // Listeners
    public void onJsonMessage(Consumer<JSONObject> onJsonMessageConsumer) {
        this.onJsonMessageConsumer = onJsonMessageConsumer;
    }

    public void onTextMessage(Consumer<String> onTextMessageConsumer) {
        this.onTextMessageConsumer = onTextMessageConsumer;
    }

    // Send helper methods

    public void send(String msg) {
        ws.sendText(msg);
    }

    public void send(JSONObject jsonObject) {
        send(jsonObject.toString());
    }

    public void send(RequestCMD cmd, Object msg) {
        JSONObject json = new JSONObject();
        json.put("cmd", cmd);
        json.put("msg", msg);
        send(json);
    }

    private void auth(String apiKey) {
        send(RequestCMD.AUTHORIZATION, apiKey);
    }

    public void subscribe(Subscribe subscribe) {
        send(RequestCMD.SUBSCRIBE, subscribe.toMap());
    }

    public void unsubscribe(Subscribe subscribe) {
        send(RequestCMD.UNSUBSCRIBE, subscribe.toMap());
    }

    public static class Subscribe {
        Set<Integer> bookmakerIds = new HashSet<>();
        Set<Integer> sportIds = new HashSet<>();

        public Subscribe bookmakerIds(Integer... bookmakerIds) {
            this.bookmakerIds.clear();

            if (bookmakerIds != null)
                this.bookmakerIds.addAll(Arrays.asList(bookmakerIds));

            return this;
        }

        public Subscribe bookmakerIds(Set<Integer> bookmakerIds) {
            this.bookmakerIds.clear();

            if (bookmakerIds != null)
                this.bookmakerIds.addAll(bookmakerIds);

            return this;
        }

        public Subscribe sportIds(Integer... sportIds) {
            this.sportIds.clear();

            if (sportIds != null)
                this.sportIds.addAll(Arrays.asList(sportIds));

            return this;
        }

        public Subscribe sportIds(Set<Integer> sportIds) {
            this.sportIds.clear();

            if (sportIds != null)
                this.sportIds = sportIds;

            return this;
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("bookmakerIds", bookmakerIds);
            if (!sportIds.isEmpty()) map.put("sportIds", sportIds);
            return map;
        }
    }

    public abstract static class Handler {
        private List<String> outcomeFields = new ArrayList<>();
        private List<String> bookmakerEventFields = new ArrayList<>();

        void handle(JSONObject jsonMsg) {
            String command = jsonMsg.optString("cmd");

            switch (command) {
                case "fields":
                    outcomeFields = jsonMsg.getJSONObject("msg").getJSONArray("Odd").toList().stream().map(x -> (String) x).collect(toList());
                    bookmakerEventFields = jsonMsg.getJSONObject("msg").getJSONArray("BookmakerEvent").toList().stream().map(x -> (String) x).collect(toList());
                    info("init fields: " + jsonMsg);
                    break;

                case "bookmaker_events":
                    for (Object raw : jsonMsg.getJSONArray("msg")) {
                        BookmakerEvent bkEvent = new BookmakerEvent(((JSONArray) raw).toList(), bookmakerEventFields);
                        bookmakerEvent(bkEvent);
                    }
                    break;

                case "outcomes":
                    Map<String, Odd> updatedOdds = new HashMap<>();

                    for (Object raw : jsonMsg.getJSONArray("msg")) {
                        Odd odd = new Odd(((JSONArray) raw).toList(), outcomeFields);
                        updatedOdds.put(odd.id, odd);
                    }

                    odds(updatedOdds);

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

                    removeBookmakerEvents(ids);
                    break;

                case "subscribed":
                    info("Subscribed successfully: " + jsonMsg);
                    break;

                case "error":
                    info("Error message: " + jsonMsg.get("msg"));
                    break;

                default:
                    info("skip command '" + command + "' with msg '" + jsonMsg.get("msg") + "'");
            }
        }

        public abstract void info(String msg);

        public abstract void bookmakerEvent(BookmakerEvent bkEvent);

        public abstract void odds(Map<String, Odd> updatedOdds);

        public abstract void removeBookmakerEvents(Collection<Long> ids);

        public abstract void onDisconnected(boolean closedByServer);
    }
}
