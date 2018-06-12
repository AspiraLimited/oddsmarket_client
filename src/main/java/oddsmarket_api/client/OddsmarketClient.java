package oddsmarket_api.client;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import oddsmarket_api.client.cmd.RequestCMD;
import oddsmarket_api.client.models.BookmakerEvent;
import oddsmarket_api.client.models.Odd;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static oddsmarket_api.client.cmd.RequestCMD.AUTHORIZATION;
import static oddsmarket_api.client.cmd.RequestCMD.SUBSCRIBE;

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
        // ws_odds.sendClose();
        // ws_odds.flush();
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
        send(AUTHORIZATION, apiKey);
    }

    public void subscribe(Subscribe subscribe) {
        send(SUBSCRIBE, subscribe.toMap());
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

    public static abstract class Handler {
        private List<String> oddFields = new ArrayList<>();
        private List<String> bookmakerEventFields = new ArrayList<>();

        void handle(JSONObject jsonMsg) {
            // logger.trace("response: {}, {}", jsonMsg.optString("cmd"), jsonMsg.opt("msg"));
            String command = jsonMsg.optString("cmd");
            // logger.info("command: {}", command);

            switch (command) {
                case "fields":
                    oddFields = jsonMsg.getJSONObject("msg").getJSONArray("Odd").toList().stream().map(x -> (String) x).collect(toList());
                    bookmakerEventFields = jsonMsg.getJSONObject("msg").getJSONArray("BookmakerEvent").toList().stream().map(x -> (String) x).collect(toList());
                    break;

                case "bookmakerevents":
                    jsonMsg.getJSONArray("msg").forEach(raw -> {
                        try {
                            bookmakerEvent(new BookmakerEvent(((JSONArray) raw).toList(), bookmakerEventFields));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                    break;

                case "odds":
                    Map<Long, Odd> odds = new HashMap<>();

                    jsonMsg.getJSONArray("msg").forEach(raw -> {
                        try {
                            Odd odd = new Odd(((JSONArray) raw).toList(), oddFields);
                            odds.put(odd.id, odd);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    odds(odds);

                    break;

                case "removed":
                    List<Long> ids = new ArrayList<>();
                    jsonMsg.getJSONObject("msg").getJSONArray("bookmakerEventIds").forEach(raw -> ids.add((long) raw));
                    removeBookmakerEvents(ids);

                    break;

                default:
                    info(jsonMsg.toString());
                    System.out.println("command " + command + " - skip");
            }
        }

        public abstract void info(String msg);

        public abstract void bookmakerEvent(BookmakerEvent bkEvent);

        public abstract void odds(Map<Long, Odd> odds);

        public abstract void removeBookmakerEvents(Collection<Long> ids);
    }
}
