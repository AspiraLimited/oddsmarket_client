package com.aspiralimited.oddsmarket.client.v4.websocket;

import com.aspiralimited.oddsmarket.api.v4.websocket.cmd.RequestCMD;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.Handler;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class OddsmarketClient {

    private final WebSocket ws;

    private Handler handler;

    private Consumer<String> onTextMessageConsumer;
    private Consumer<JSONObject> onJsonMessageConsumer;

    // TODO timeouts
    private OddsmarketClient(String websocketUrl) throws IOException {
        this.ws = new WebSocketFactory().createSocket(websocketUrl);

        ws.addListener(new WebSocketAdapter() {
            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                handler.onDisconnected(closedByServer);
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            }

            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                handleTextMessage(message);
            }
        });
    }

    void handleTextMessage(String message) {
        try {
            if (onTextMessageConsumer != null) {
                onTextMessageConsumer.accept(message);
            }

            JSONObject json = new JSONObject(message);
            if (onJsonMessageConsumer != null) {
                onJsonMessageConsumer.accept(json);
            }

            if (handler != null) {
                handler.handle(json);
            }
        } catch (Exception e) {
            if (handler != null) {
                handler.error("Websocket message handler exception. Incoming message: " + message, e);
            }
        }
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
        send(RequestCMD.SUBSCRIBE, new JSONObject(subscribe.toMap()));
    }

    public void unsubscribe(Subscribe subscribe) {
        send(RequestCMD.UNSUBSCRIBE, new JSONObject(subscribe.toMap()));
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

}
