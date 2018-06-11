package oddsmarket_api.client;

import com.allbestbets.jutils.AbbLogger;
import oddsmarket_api.client.models.BookmakerEvent;
import oddsmarket_api.client.models.Odd;

import java.util.Collection;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Demo {
    private static final AbbLogger logger = new AbbLogger();

    public static void main(String[] args) throws Exception {
        OddsmarketClient client = OddsmarketClient.connect("wss://api-pr.oddsmarket.org/v2/odds_ws", "fd87117fff57764e8717ed0c6b0f702a");

        client.onJsonMessage(jsonMsg -> {
            System.out.println("response: " + jsonMsg.optString("cmd") + " " + jsonMsg.opt("msg"));
        });

        client.handler(new OddsmarketClient.Handler() {

            @Override
            public void info(String msg) {
                logger.info("info {}", msg);
            }

            @Override
            public void bookmakerEvent(BookmakerEvent bkEvent) {
                logger.trace("bookmaker event {}", bkEvent);
            }

            @Override
            public void odds(Map<Long, Odd> updatedOdds) {
                logger.debug("odds {}", updatedOdds.size());
            }

            @Override
            public void removeBookmakerEvents(Collection<Long> ids) {
                logger.debug("remove bookmaker events {}", ids);
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
}
