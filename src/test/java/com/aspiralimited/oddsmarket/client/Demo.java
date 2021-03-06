package com.aspiralimited.oddsmarket.client;

import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;
import com.aspiralimited.oddsmarket.client.models.Odd;

import java.util.Collection;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Demo {

    public static void main(String[] args) throws Exception {
        OddsmarketClient client = OddsmarketClient.connect("wss://api-pr.oddsmarket.org/v3/odds_ws", "API_KEY");

        client.onJsonMessage(jsonMsg -> {
            System.out.println("response: " + jsonMsg.optString("cmd") + " " + jsonMsg.opt("msg"));
        });

        OddsmarketClient.Handler handler = new OddsmarketClient.Handler() {

            @Override
            public void info(String msg) {
                printToConsole("info " + msg);
            }

            @Override
            public void bookmakerEvent(BookmakerEvent bkEvent) {
                printToConsole("bookmaker event " + bkEvent);
            }

            @Override
            public void odds(Map<String, Odd> updatedOdds) {
                printToConsole("odds " + updatedOdds.size());
            }

            @Override
            public void removeBookmakerEvents(Collection<Long> ids) {
                printToConsole("remove bookmaker events " + ids);
            }

            @Override
            public void onDisconnected(boolean closedByServer) {

            }
        };

        client.handler(handler);

        OddsmarketClient.Subscribe subscribe = new OddsmarketClient.Subscribe()
                .bookmakerIds(2)
                .sportIds(2);

        client.subscribe(subscribe);

        sleep(5_000);

        client.unsubscribe(subscribe);

        System.out.println(client.bookmakerEvents());
        System.out.println(client.odds().size());

        subscribe = new OddsmarketClient.Subscribe()
                .bookmakerIds(21)
                .sportIds(2);

        client.subscribe(subscribe);

        sleep(5_000);

        System.out.println(client.bookmakerEvents());
        System.out.println(client.odds().size());

        client.disconnect();

        sleep(3_000);
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
