package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.InteractiveTradingFeedReaderPrompter;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderCliParser;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;

public class TradingFeedReader {

    public static void main(String[] args) {
        try {
            TradingFeedReaderCliParser cliParser = new TradingFeedReaderCliParser();
            TradingFeedReaderConfiguration configuration;
            if (cliParser.isInteractiveMode(args)) {
                configuration = new InteractiveTradingFeedReaderPrompter().prompt();
            } else {
                if (args.length < 2) {
                    printUsage();
                    System.exit(1);
                    return;
                }
                configuration = cliParser.parse(args);
            }

            new TradingFeedReaderRunner().run(configuration);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(1);
        }
    }

    private static void printUsage() {
        printToConsole("Required command-line arguments are missing!");
        printToConsole("Usage examples:");
        printToConsole("tradingfeedreader.sh api-pr.oddsmarket.org BOOKMAKER-ID [SPORT-ID1,SPORT-ID2,...] [--apiKey VALUE | --apiKeyFile path] [--saveMessagesToFolder path] [--groupMessagesByEvent true|false] [--recordOnlyEventIds id1,id2,...] [--recordOnlyRawEventIds raw1,raw2,...]");
        printToConsole("If neither --apiKey nor --apiKeyFile is given, the API key is read from ./api-token.txt by default.");
        printToConsole("If --saveMessagesToFolder is not specified, messages are written to ./data by default.");
        printToConsole("tradingfeedreader.sh --interactive");
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
