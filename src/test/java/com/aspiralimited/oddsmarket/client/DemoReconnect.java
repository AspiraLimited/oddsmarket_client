package com.aspiralimited.oddsmarket.client;

import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;
import com.aspiralimited.oddsmarket.client.models.Odd;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;

class DemoReconnect {

    public static final String API_KEY = "API_KEY";

    public static void main(String[] args) throws IOException, WebSocketException, InterruptedException {
        OddsmarketClient client = OddsmarketClient.connect("wss://api-pr.oddsmarket.org/v3/odds_ws", API_KEY);
        client.onJsonMessage(x -> {
            System.out.println(x);
        });
        OddsmarketClient.Handler handler = new OddsmarketClient.Handler() {
            @Override
            public void info(String msg) {
                System.out.println(msg.substring(0, Math.min(128, msg.length()-1)));
            }
            @Override
            public void bookmakerEvent(BookmakerEvent bookmakerEvent) {
                // if (bookmakerEvent.id == eventId)
                System.out.println("" + currentTimeMillis() + ": " + bookmakerEvent);
            }
            @Override
            public void odds(Map<String, Odd> map) {
//                if (map.values().stream().findFirst().get().bookmakerEventId == eventId)
                System.out.println("" + currentTimeMillis() + ": " + map.size());
            }
            @Override
            public void removeBookmakerEvents(Collection<Long> collection) {
            }
        };
        client.handler(handler);
        OddsmarketClient.Subscribe sub = new OddsmarketClient.Subscribe().bookmakerIds(4).sportIds(18);
        client.subscribe(sub);
        System.out.println("----");
        System.out.println(client.bookmakerEvents().size());
        System.out.println(client.odds().size());
        sleep(2_000);
        client.disconnect();

        client = OddsmarketClient.connect("wss://api-pr.oddsmarket.org/v3/odds_ws", API_KEY);
        client.onJsonMessage(x -> {
            System.out.println(x);
        });
        client.handler(handler);

        System.out.println("-- reconnected --");
        System.out.println(client.bookmakerEvents().size());
        System.out.println(client.odds().size());
        client.subscribe(sub);
        System.out.println("-- re-subscribed --");
        sleep(2_000);
        System.out.println(client.bookmakerEvents().size());
        System.out.println(client.odds().size());
        sleep(2_000);
        client.disconnect();
        System.exit(0);
    }
}

