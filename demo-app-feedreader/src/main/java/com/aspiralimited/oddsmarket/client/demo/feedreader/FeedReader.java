package com.aspiralimited.oddsmarket.client.demo.feedreader;

import com.aspiralimited.oddsmarket.client.v4.websocket.OddsmarketClient;
import com.aspiralimited.oddsmarket.client.v4.rest.OddsmarketRestHttpClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class FeedReader {

    public static void main(String[] args) throws Exception {
        try {
            if (args.length < 3) {
                printToConsole("Required command-line arguments are missing!");
                printToConsole("Usage example:");
                printToConsole("feedreader.sh api-pr.oddsmarket.org YOUR-API-KEY BOOKMAKER-ID [SPORT-ID1,SPORT-ID2,...]");
                System.exit(1);
            }

            String feedDomain = args[0];
            if (feedDomain.isEmpty()) {
                throw new IllegalArgumentException("Feed domain key must be specified as first argument in command-line parameters");
            }
            String apiKey = args[1];
            if (apiKey.isEmpty()) {
                throw new IllegalArgumentException("API key must be specified in command-line parameters");
            }
            int bookmakerId = Integer.parseInt(args[2]);
            Set<Integer> sportIds = Collections.emptySet();
            if (args.length >= 4) {
                String sportIdsStr = args[3];
                sportIds = Arrays.stream(sportIdsStr.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
            }
            String feedWebsocketUrl = (feedDomain.startsWith("localhost") ? "ws://" : "wss://") + feedDomain + "/v3/odds_ws";
            OddsmarketClient client = OddsmarketClient.connect(feedWebsocketUrl, apiKey);
            OddsmarketRestHttpClient oddsmarketRestHttpClient = new OddsmarketRestHttpClient(
                    "https://api-mst.oddsmarket.org",
                    (feedDomain.startsWith("localhost") ? "http://" : "https://") + feedDomain,
                    5000L
            );
            DictionariesService dictionariesService = new DictionariesService(oddsmarketRestHttpClient);
            DiffPrinter listener = new DiffPrinter(client, dictionariesService);
            listener.listenFeedAndPrintDiffs(bookmakerId, sportIds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(1);
        }
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
