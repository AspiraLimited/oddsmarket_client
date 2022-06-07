package com.aspiralimited.oddsmarket.client.demo.basic;

import com.aspiralimited.oddsmarket.client.OddsmarketClient;
import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;
import com.aspiralimited.oddsmarket.client.models.Odd;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class DemoConnectionStabilityMultithreaded {

    private static final ThreadGroup group = new ThreadGroup("Sockets ");


    public static void main(String[] args) {
        int[] bkIds = new int[]{3, 31, 48, 1, 4, 13, 79, 6, 53, 10, 19, 17, 2, 11, 5, 32};
        for (int bkId : bkIds) {
            Thread thread = toThread(bkId);
            thread.start();
        }
    }


    private static Thread toThread(int bkId) {
        String threadName = String.valueOf(bkId);
        return new Thread(group, () -> {
            try {
                doTest(bkId);
            } catch (IOException | WebSocketException | InterruptedException e) {
                e.printStackTrace();
            }
        }, threadName);
    }


    private static void doTest(int bkId) throws IOException, WebSocketException, InterruptedException {

        AtomicLong msgsReceived = new AtomicLong();
        AtomicLong duration = new AtomicLong();
        AtomicBoolean isDisconnected = new AtomicBoolean();

        long start = System.currentTimeMillis();
        OddsmarketClient client = OddsmarketClient.connect("wss://api-lv.oddsmarket.org/v3/odds_ws", "API_KEY");

        client.onJsonMessage(jsonMsg -> {
            msgsReceived.incrementAndGet();
        });

        OddsmarketClient.Handler handler = new OddsmarketClient.Handler() {

            @Override
            public void info(String msg) {
                printToConsole("info " + msg);
            }

            @Override
            public void bookmakerEvent(BookmakerEvent bkEvent) {
            }

            @Override
            public void odds(Map<String, Odd> updatedOdds) {
            }

            @Override
            public void removeBookmakerEvents(Collection<Long> ids) {
            }

            @Override
            public void onDisconnected(boolean closedByServer) {
                duration.set(System.currentTimeMillis() - start);
                isDisconnected.set(true);
                printToConsole("disconnected! closedByServer=" + closedByServer + "; bkId=" + bkId + "; connection duration (ms)=" + duration.get());
            }
        };

        client.handler(handler);
        OddsmarketClient.Subscribe subscribe = new OddsmarketClient.Subscribe()
                .bookmakerIds(bkId);

        client.subscribe(subscribe);

        for (int i = 0; i < 100000; i++) {
            if (isDisconnected.get()) {
                System.out.println(new Date() + ": bkId=" + bkId + ": disconnected. Connection duration (ms): " + duration.get());
            } else {
                System.out.println(new Date() + ": bkId=" + bkId + ": " + msgsReceived.get() + " messages received");
            }
            sleep(60_000);
        }

        client.unsubscribe(subscribe);
        client.disconnect();
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
