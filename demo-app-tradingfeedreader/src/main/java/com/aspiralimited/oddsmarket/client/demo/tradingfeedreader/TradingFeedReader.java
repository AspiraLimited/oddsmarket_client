package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader;

import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli.TradingFeedReaderCommand;
import picocli.CommandLine;

public class TradingFeedReader {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new TradingFeedReaderCommand()).execute(args);
        System.exit(exitCode);
    }
}
