package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.InteractiveTradingFeedReaderPrompter;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderCliParser;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderConfiguration;

public class TradingFeedReader {

    public static void main(String[] args) {
        try {
            TradingFeedReaderCliParser cliParser = new TradingFeedReaderCliParser();
            if (cliParser.isHelpRequested(args)) {
                printUsage();
                System.exit(0);
                return;
            }
            TradingFeedReaderConfiguration configuration;
            if (cliParser.isInteractiveMode(args)) {
                configuration = new InteractiveTradingFeedReaderPrompter().prompt();
            } else {
                if (args.length == 0) {
                    printUsage();
                    System.exit(3);
                    return;
                }
                try {
                    configuration = cliParser.parse(args);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: " + e.getMessage());
                    System.err.println();
                    printUsage();
                    System.exit(3);
                    return;
                }
            }

            // run() blocks forever; final exit code is set by the runner's shutdown hook (Runtime.halt).
            new TradingFeedReaderRunner().run(configuration);
        } catch (Exception e) {
            // Reached only for exceptions before the shutdown hook is registered (e.g., recorder construction).
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        printToConsole("Usage:");
        printToConsole("  tradingfeedreader.sh --feedDomain=<host> --tradingFeedId=<id> [options...]");
        printToConsole("  tradingfeedreader.sh --interactive");
        printToConsole("  tradingfeedreader.sh --help  (or -h)");
        printToConsole("");
        printToConsole("Required:");
        printToConsole("  --feedDomain=<host>          e.g. api-pr.oddsmarket.org or api-lv.oddsmarket.org");
        printToConsole("  --tradingFeedId=<id>         numeric Trading Feed ID");
        printToConsole("");
        printToConsole("Subscription options:");
        printToConsole("  --sportIds=<id1,id2,...>     filter events by sport (default: all sports)");
        printToConsole("  --locales=<en,ru,...>        comma-separated locale codes (default: en)");
        printToConsole("  --rawIdOriginBookmakerId=<n> include rawEventId from this bookmaker in messages");
        printToConsole("  --fillRawOutcomeId=true|false");
        printToConsole("  --fillDirectLink=true|false");
        printToConsole("");
        printToConsole("Authentication (in priority order):");
        printToConsole("  --apiKey=<value>             literal API key");
        printToConsole("  --apiKeyFile=<path>          read API key from file");
        printToConsole("  (default)                    read API key from ./api-key.txt");
        printToConsole("");
        printToConsole("Recording options:");
        printToConsole("  --saveMessagesToFolder=<path>      default: ./data");
        printToConsole("  --groupMessagesByEvent=true|false  prefix message filenames with eventId (default: false)");
        printToConsole("  --recordOnlyEventIds=<id1,...>     strict filter: only record these OddsMarket event IDs");
        printToConsole("  --recordOnlyRawEventIds=<id1,...>  strict filter by bookmaker IDs (requires --rawIdOriginBookmakerId)");
        printToConsole("");
        printToConsole("Run control:");
        printToConsole("  --duration=<30s|5m|1h>       stop gracefully after this time");
        printToConsole("  --maxMessages=<n>            stop gracefully after this many messages recorded to disk");
        printToConsole("");
        printToConsole("Examples:");
        printToConsole("  tradingfeedreader.sh --feedDomain=api-lv.oddsmarket.org --tradingFeedId=500 --duration=2m");
        printToConsole("  tradingfeedreader.sh --feedDomain=api-pr.oddsmarket.org --tradingFeedId=500 --recordOnlyEventIds=12345 --duration=5m");
        printToConsole("  tradingfeedreader.sh --interactive");
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
