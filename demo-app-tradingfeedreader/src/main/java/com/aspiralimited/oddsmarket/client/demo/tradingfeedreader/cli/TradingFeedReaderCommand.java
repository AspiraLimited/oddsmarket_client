package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.cli;

import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.TradingFeedReaderRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(
        name = "tradingfeedreader",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        usageHelpAutoWidth = true,
        exitCodeOnInvalidInput = 3,
        exitCodeOnUsageHelp = 0,
        exitCodeOnExecutionException = 1,
        description = "Subscribes to an OddsMarket trading feed and records every message to disk.%n",
        footerHeading = "%nExamples:%n",
        footer = {
                "  tradingfeedreader --feedDomain=api-lv.oddsmarket.org --tradingFeedId=500 --duration=2m",
                "  tradingfeedreader --feedDomain=api-pr.oddsmarket.org --tradingFeedId=500 --recordOnlyEventIds=12345 --duration=5m",
                "  tradingfeedreader --interactive"
        }
)
public class TradingFeedReaderCommand implements Callable<Integer> {

    @Option(
            names = "--interactive",
            description = "Prompt for parameters interactively instead of using CLI flags."
    )
    private boolean interactive;

    @Option(
            names = "--feedDomain",
            description = "Required (unless --interactive). e.g. api-pr.oddsmarket.org (prematch) or api-lv.oddsmarket.org (live)."
    )
    private String feedDomain;

    @Option(
            names = "--tradingFeedId",
            description = "Required (unless --interactive). Numeric Trading Feed ID provided by OddsMarket."
    )
    private Short tradingFeedId;

    @Option(
            names = "--sportIds",
            split = ",",
            description = "Filter the subscription by sport IDs (default: all sports)."
    )
    private Set<Short> sportIds;

    @Option(
            names = "--locales",
            split = ",",
            description = "Comma-separated locale codes (default: server default, typically 'en')."
    )
    private Set<String> locales;

    @Option(
            names = "--rawIdOriginBookmakerId",
            description = "Include the bookmaker's own rawEventId (from this bookmaker) in each message."
    )
    private Short rawIdOriginBookmakerId;

    @Option(
            names = "--fillRawOutcomeId",
            description = "Include rawOutcomeId in outcomes (default: server default)."
    )
    private Boolean fillRawOutcomeId;

    @Option(
            names = "--fillDirectLink",
            description = "Include directLink URLs in event metadata (default: server default)."
    )
    private Boolean fillDirectLink;

    @Option(
            names = "--apiKey",
            description = "Literal API key. Highest priority — beats --apiKeyFile and the default api-key.txt."
    )
    private String apiKey;

    @Option(
            names = "--apiKeyFile",
            description = "Read the API key from this file. If neither --apiKey nor --apiKeyFile is set, the tool reads ./api-key.txt by default."
    )
    private Path apiKeyFile;

    @Option(
            names = "--saveMessagesToFolder",
            description = "Parent folder for the session subfolder (default: ./data)."
    )
    private Path saveMessagesToFolder;

    @Option(
            names = "--groupMessagesByEvent",
            description = "Prefix message filenames with eventId (default: false)."
    )
    private Boolean groupMessagesByEvent;

    @Option(
            names = "--recordOnlyEventIds",
            split = ",",
            description = "Strict filter: only record these OddsMarket event IDs. Non-event messages are dropped."
    )
    private Set<Long> recordOnlyEventIds;

    @Option(
            names = "--recordOnlyRawEventIds",
            split = ",",
            description = "Strict filter by bookmaker IDs (requires --rawIdOriginBookmakerId)."
    )
    private Set<String> recordOnlyRawEventIds;

    @Option(
            names = "--duration",
            converter = FriendlyDurationConverter.class,
            description = "Stop gracefully after this much time (format: <number><s|m|h>, e.g. 30s, 5m, 1h)."
    )
    private Duration duration;

    @Option(
            names = "--maxMessages",
            description = "Stop gracefully after this many messages have been recorded to disk."
    )
    private Long maxMessages;

    @Override
    public Integer call() {
        TradingFeedReaderConfiguration configuration = interactive
                ? new InteractiveTradingFeedReaderPrompter().prompt()
                : buildConfiguration();
        // run() blocks forever; the final exit code is set by the runner's shutdown hook via Runtime.halt().
        new TradingFeedReaderRunner().run(configuration);
        return 0;
    }

    private TradingFeedReaderConfiguration buildConfiguration() {
        if (feedDomain == null || feedDomain.isBlank()) {
            throw new IllegalArgumentException("--feedDomain is required (unless --interactive is used).");
        }
        if (tradingFeedId == null) {
            throw new IllegalArgumentException("--tradingFeedId is required (unless --interactive is used).");
        }

        String resolvedApiKey = ApiKeyResolver.resolve(apiKey, apiKeyFile);
        Path resolvedFolder = saveMessagesToFolder != null
                ? saveMessagesToFolder
                : java.nio.file.Paths.get(com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.Constants.DEFAULT_SAVE_MESSAGES_FOLDER);
        if (maxMessages != null && maxMessages <= 0) {
            throw new IllegalArgumentException("--maxMessages must be a positive number, got: " + maxMessages);
        }

        return TradingFeedReaderConfiguration.builder()
                .feedDomain(feedDomain)
                .apiKey(resolvedApiKey)
                .tradingFeedId(tradingFeedId)
                .sportIds(sportIds)
                .saveMessagesToFolder(resolvedFolder)
                .groupMessagesByEvent(groupMessagesByEvent != null && groupMessagesByEvent)
                .locales(locales)
                .rawIdOriginBookmakerId(rawIdOriginBookmakerId)
                .fillRawOutcomeId(fillRawOutcomeId)
                .fillDirectLink(fillDirectLink)
                .recordOnlyEventIds(recordOnlyEventIds)
                .recordOnlyRawEventIds(recordOnlyRawEventIds)
                .duration(duration)
                .maxMessages(maxMessages)
                .build();
    }

    public static class FriendlyDurationConverter implements ITypeConverter<Duration> {
        @Override
        public Duration convert(String value) {
            return CliValueParsers.parseDuration(value);
        }
    }
}
