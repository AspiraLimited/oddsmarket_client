package com.aspiralimited.oddsmarket.client;

import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;
import com.aspiralimited.oddsmarket.client.models.Odd;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static java.lang.Thread.sleep;

public class DemoConnectionStability {
    static volatile long msgsReceived = 0;

    public static void main(String[] args) throws Exception {
        OddsmarketClient client = OddsmarketClient.connect("wss://api-pr.oddsmarket.org/v3/odds_ws", "API_KEY");

        client.onJsonMessage(jsonMsg -> {
            //System.out.println("response: " + jsonMsg.optString("cmd") + " " + jsonMsg.opt("msg"));
            msgsReceived++;
        });

        OddsmarketClient.Handler handler = new OddsmarketClient.Handler() {

            @Override
            public void info(String msg) {
                printToConsole("info " + msg);
            }

            @Override
            public void bookmakerEvent(BookmakerEvent bkEvent) {

                //printToConsole("bookmaker event " + bkEvent);
            }

            @Override
            public void odds(Map<String, Odd> updatedOdds) {

                //printToConsole("odds " + updatedOdds.size());
            }

            @Override
            public void removeBookmakerEvents(Collection<Long> ids) {
                // printToConsole("remove bookmaker events " + ids);
            }
        };

        client.handler(handler);
        Random random = new Random();
        int[] bkIds = new int[]{3, 31, 48, 1, 4, 13, 79, 6, 53, 10, 19, 17, 2, 11, 5, 32};
        int bkId = bkIds[random.nextInt(bkIds.length)];
        OddsmarketClient.Subscribe subscribe = new OddsmarketClient.Subscribe()
                .bookmakerIds(bkId);

        client.subscribe(subscribe);

        for (int i = 0; i < 3600; i++) {
            System.out.println(new Date() + ": " + msgsReceived + " messages received");
            sleep(10_000);
        }

        client.unsubscribe(subscribe);

        System.out.println(client.bookmakerEvents());
        System.out.println(client.odds().size());

        client.disconnect();

        sleep(3_000);
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
