package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.InteractiveTradingFeedReaderPrompter;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderCliParser;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;
import com.aspiralimited.oddsmarket.client.v4.rest.OddsmarketRestHttpClient;

public class TradingFeedReader {

    public static void main(String[] args) {
        try {
            TradingFeedReaderCliParser cliParser = new TradingFeedReaderCliParser();
            TradingFeedReaderConfiguration configuration;
            if (cliParser.isInteractiveMode(args)) {
                configuration = new InteractiveTradingFeedReaderPrompter().prompt();
            } else {
                if (args.length < 3) {
                    printUsage();
                    System.exit(1);
                    return;
                }
                configuration = cliParser.parse(args);
            }

            OddsmarketRestHttpClient oddsmarketRestHttpClient = new OddsmarketRestHttpClient(
                    "https://api-mst.oddsmarket.org",
                    (configuration.getFeedDomain().startsWith("localhost") ? "http://" : "https://") + configuration.getFeedDomain(),
                    5000L
            );
            DiffPrinter listener = new DiffPrinter(oddsmarketRestHttpClient);
            listener.listenFeedAndPrintDiffs(configuration);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(1);
        }
    }

    private static void printUsage() {
        printToConsole("Required command-line arguments are missing!");
        printToConsole("Usage examples:");
        printToConsole("tradingfeedreader.sh api-pr.oddsmarket.org YOUR-API-KEY BOOKMAKER-ID [SPORT-ID1,SPORT-ID2,...] [--saveMessagesToFolder path]");
        printToConsole("tradingfeedreader.sh --interactive");
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
