package oddsmarket_api.client;

import oddsmarket_api.client.models.BookmakerEvent;
import oddsmarket_api.client.models.Odd;

import java.util.Collection;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Demo {

    public static void main(String[] args) throws Exception {
        OddsmarketClient client = OddsmarketClient.connect("wss://api-pr.oddsmarket.org/v2/odds_ws", "APP_KEY");

        client.onJsonMessage(jsonMsg -> {
            System.out.println("response: " + jsonMsg.optString("cmd") + " " + jsonMsg.opt("msg"));
        });

        client.handler(new OddsmarketClient.Handler() {

            @Override
            public void info(String msg) {
                printToConsole("info " + msg);
            }

            @Override
            public void bookmakerEvent(BookmakerEvent bkEvent) {
                printToConsole("bookmaker event " + bkEvent);
            }

            @Override
            public void odds(Map<Long, Odd> updatedOdds) {
                printToConsole("odds " + updatedOdds.size());
            }

            @Override
            public void removeBookmakerEvents(Collection<Long> ids) {
                printToConsole("remove bookmaker events " + ids);
            }
        });

        OddsmarketClient.Subscribe subscribe = new OddsmarketClient.Subscribe()
                .bookmakerIds(2)
                .sportIds(2);

        client.subscribe(subscribe);

        sleep(3600_000);

        client.disconnect();

        sleep(3_000);
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
