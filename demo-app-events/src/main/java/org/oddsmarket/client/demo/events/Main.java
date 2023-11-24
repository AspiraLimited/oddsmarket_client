package org.oddsmarket.client.demo.events;

import com.aspiralimited.oddsmarket.client.wsevents.OddsmarketClient;
import com.aspiralimited.oddsmarket.client.wsevents.OddsmarketEventsListener;
import com.aspiralimited.oddsmarket.client.wsevents.OddsmarketHandler;
import com.aspiralimited.oddsmarket.client.wsevents.dto.Event;
import com.aspiralimited.oddsmarket.client.wsevents.dto.EventLiveInfo;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            log.error("Usage: demo-app-events <url> <api-key> [sport-id-1,sport-id-2,...]");
            System.exit(1);
        }

        String url = args[0];
        String apiKey = args[1];

        if (apiKey.isEmpty()) {
            log.error("API key cannot be empty");
            System.exit(1);
        }

        if (url.isEmpty()) {
            log.error("URL cannot be empty");
            System.exit(1);
        }

        OddsmarketClient.Options.OptionsBuilder optionsBuilder = OddsmarketClient.Options.builder()
                .websocketUrl(url)
                .apiKey(apiKey);

        if (args.length == 3) {
            try {
                optionsBuilder.sportIds(Arrays.stream(args[2].split(","))
                        .map(Short::parseShort).collect(Collectors.toSet()));
            } catch (NumberFormatException e) {
                log.error("Invalid sport ids format: {}", args[2]);
                System.exit(1);
            }
        }

        OddsmarketClient client = new OddsmarketClient(optionsBuilder.build());
        OddsmarketHandler handler = createHandler(client);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(() -> dumpStatistic(handler), 10, 10, TimeUnit.SECONDS);

        try {
            client.connect();
        } catch (Exception e) {
            log.error("Error connecting to WebSocket", e);
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            client.disconnect();
            handler.shutdown();
            scheduledExecutorService.shutdownNow();
        }));
    }

    private static OddsmarketHandler createHandler(OddsmarketClient client) {
        OddsmarketHandler handler = new OddsmarketHandler(client);

        handler.addEventsListener(new OddsmarketEventsListener() {

            @Override
            public void onEvent(Event event) {
                log.trace("Event: {}", event);
            }

            @Override
            public void onEventDeleted(Event event) {
                log.trace("Event deleted: {}", event);
            }

            @Override
            public void onEventLiveInfo(EventLiveInfo eventLiveInfo) {
                log.trace("Event live info: {}", eventLiveInfo);
            }
        });

        return handler;
    }

    private static void dumpStatistic(OddsmarketHandler handler) {
        log.info("Statistic: [eventMapSize={}, eventLiveInfoMapSize={}]",
                handler.getEventMap().size(), handler.getEventLiveInfoMap().size());
    }
}
