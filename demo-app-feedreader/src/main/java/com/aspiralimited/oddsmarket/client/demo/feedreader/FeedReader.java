package com.aspiralimited.oddsmarket.client.demo.feedreader;

import com.aspiralimited.oddsmarket.client.OddsmarketClient;
import com.aspiralimited.oddsmarket.client.rest.OddsmarketRestHttpClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FeedReader {

    public static void main(String[] args) throws Exception {
        try {
            if (args.length < 3) {
                printToConsole("Required command-line arguments are missing!");
                printToConsole("Usage example:");
                printToConsole("feedreader.sh wss://api-pr.oddsmarket.org/v3/odds_ws YOUR-API-KEY BOOKMAKER-ID [SPORT-ID1,SPORT-ID2,...]");
                System.exit(1);
            }
            String feedUrl = args[0];
            if (feedUrl.isEmpty()) {
                throw new IllegalArgumentException("Feed URL key must be specified as first argument in command-line parameters");
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
            OddsmarketClient client = OddsmarketClient.connect(feedUrl, apiKey);
            OddsmarketRestHttpClient oddsmarketRestHttpClient = new OddsmarketRestHttpClient(
                    "https://api-mst.oddsmarket.org",
                    //"http://localhost:3000",
                    "https://api-lv.oddsmarket.org",
                    "https://api-pr.oddsmarket.org",
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
