package com.aspiralimited.oddsmarket.client.demo.feedreader;

import com.aspiralimited.config.statical.Period;
import com.aspiralimited.oddsmarket.client.demo.feedreader.state.OutcomeDataDiffDetector;
import com.aspiralimited.oddsmarket.client.websocket.handlers.Handler;
import com.aspiralimited.oddsmarket.client.websocket.OddsmarketClient;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.DiffList;
import com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator.BetSpecCache;
import com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator.OutcomeNameTranslator;
import com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator.ReverseBetSpecCalculator;
import com.aspiralimited.oddsmarket.client.demo.feedreader.state.BookmakerEventStateDiffDetector;
import com.aspiralimited.oddsmarket.client.demo.feedreader.state.InMemoryStateStorage;
import com.aspiralimited.oddsmarket.client.websocket.handlers.statekeeping.StateKeepingHandler;
import com.aspiralimited.oddsmarket.client.websocket.handlers.statekeeping.model.BookmakerEventState;
import com.aspiralimited.oddsmarket.client.websocket.handlers.statekeeping.model.OutcomeData;
import com.aspiralimited.oddsmarket.client.websocket.handlers.statekeeping.model.OutcomeKey;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.BookmakerEventDto;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;
import lombok.SneakyThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static java.lang.Thread.sleep;

public class DiffPrinter {
    private final OddsmarketClient client;
    private final DictionariesService dictionariesService;
    private final InMemoryStateStorage inMemoryStateStorage = new InMemoryStateStorage();
    private final OutcomeNameTranslator outcomeNameTranslator;

    static private final DateFormat matchStartTimeFormat = new SimpleDateFormat("MMM d HH:mm", Locale.ENGLISH);

    public DiffPrinter(OddsmarketClient client, DictionariesService dictionariesService) {
        this.client = client;
        this.dictionariesService = dictionariesService;
        ReverseBetSpecCalculator reverseBetSpecCalculator = new ReverseBetSpecCalculator(
                dictionariesService.getMarketAndBetTypeDtoById(),
                dictionariesService.getBetTypeDtoById(),
                new BetSpecCache()
        );
        outcomeNameTranslator = new OutcomeNameTranslator(
                dictionariesService.getMarketAndBetTypeDtoById(),
                ResourceBundle.getBundle(OutcomeNameTranslator.RESOURCE_BUNDLE_NAME),
                reverseBetSpecCalculator
        );

    }

    @SneakyThrows
    public void listenFeedAndPrintDiffs(int bookmakerId, Set<Integer> sportIds) {

        Handler handler = new StateKeepingHandler() {

            @Override
            public void info(String msg) {
                printToConsole("Info: " + msg);
            }

            @Override
            public void bookmakerEvent(BookmakerEventDto bkEvent) {
                long bookmakerEventId = bkEvent.id;
                String bookmakerEventIdName = getBookmakerEventIdName(bkEvent);
                if (inMemoryStateStorage.hasBookmakerEvent(bookmakerEventId)) {
                    BookmakerEventState bookmakerEventState = inMemoryStateStorage.getBookmakerEvent(bookmakerEventId);

                    DiffList diffList = BookmakerEventStateDiffDetector.getStateVsDtoDiff(bookmakerEventState, bkEvent);
                    bookmakerEventState.updateProperties(bkEvent);
                    if (!diffList.isEmpty()) {
                        printToConsole("[UPD] " + bookmakerEventIdName + ": " + diffList.toString());
                    }
                } else {
                    inMemoryStateStorage.putBookmakerEvent(bkEvent);
                    printToConsole("[NEW] " + getBookmakerEventIdName(bkEvent) + " " + bkEvent);
                }
            }

            @Override
            public void outcomes(List<OutcomeDto> updatedOutcomes) {
                Map<Long, List<OutcomeDto>> oddsByBkEventId = new HashMap<>();
                for (OutcomeDto outcomeDto : updatedOutcomes) {
                    oddsByBkEventId
                            .computeIfAbsent(outcomeDto.bookmakerEventId, id -> new ArrayList<>())
                            .add(outcomeDto);
                }
                for (Map.Entry<Long, List<OutcomeDto>> bkEventIdAndOdds : oddsByBkEventId.entrySet()) {
                    long bookmakerEventId = bkEventIdAndOdds.getKey();
                    if (inMemoryStateStorage.hasBookmakerEvent(bookmakerEventId)) {
                        BookmakerEventState bookmakerEventState = inMemoryStateStorage.getBookmakerEvent(bookmakerEventId);
                        String bookmakerEventIdName = getBookmakerEventIdName(bookmakerEventState);
                        printToConsole("[ODDS] " + bookmakerEventIdName);
                        for (OutcomeDto outcomeDto : bkEventIdAndOdds.getValue()) {
                            OutcomeKey outcomeKey = new OutcomeKey(outcomeDto.marketAndBetTypeId,
                                    outcomeDto.marketAndBetTypeParameterValue,
                                    (short) (int) outcomeDto.periodIdentifier,
                                    outcomeDto.oddsLay != null && outcomeDto.oddsLay != 0,
                                    outcomeDto.playerId1,
                                    outcomeDto.playerId2
                            );
                            String outcomeName = generateOutcomeName(outcomeKey, bookmakerEventState.isSwapTeams(), (short) bookmakerEventState.getSportId());
                            if (bookmakerEventState.hasOutcome(outcomeKey)) {
                                if (outcomeDto.active()) {
                                    DiffList diffList = OutcomeDataDiffDetector.updatePropertiesAndReturnDiff(bookmakerEventState.getOutcome(outcomeKey), outcomeDto);
                                    bookmakerEventState.putOutcome(outcomeKey, outcomeDto);
                                    if (!diffList.isEmpty()) {
                                        printToConsole("    [UPD] " + outcomeName + ": " + diffList);
                                    }
                                    bookmakerEventState.putOutcome(outcomeKey, outcomeDto);
                                } else {
                                    // Deactivated outcomes must be removed
                                    bookmakerEventState.removeOutcome(outcomeKey);
                                    printToConsole("    [DEL] " + outcomeName);
                                }
                            } else {
                                if (outcomeDto.active()) {
                                    OutcomeData newOutcomeData = bookmakerEventState.putOutcome(outcomeKey, outcomeDto);
                                    printToConsole("    [NEW] " + outcomeName + ": " + OutcomeDataDiffDetector.outcomeDataToShortString(newOutcomeData));
                                } else {
                                    throw new IllegalStateException("Missing outcome is being deactivated. ID=" + outcomeDto.id);
                                }
                            }
                        }
                    } else {
                        throw new IllegalStateException("Missing bookmaker event information for id=" + bookmakerEventId);
                    }

                }

            }

            @Override
            public void removeBookmakerEvents(Collection<Long> ids) {
                for (Long bookmakerEventId : ids) {
                    BookmakerEventState bkEvent = inMemoryStateStorage.removeBookmakerEvent(bookmakerEventId);
                    if (bkEvent != null) {
                        printToConsole("[DEL] " + bkEvent);
                    } else {
                        throw new IllegalStateException("Missing bookmaker event information when trying to remove bookmaker event id=" + bookmakerEventId);
                    }
                }
            }

            @Override
            public void onDisconnected(boolean closedByServer) {
                printToConsole("Connection lost");
                System.exit(0);
            }
        };

        client.handler(handler);

        OddsmarketClient.Subscribe subscribe = new OddsmarketClient.Subscribe()
                .bookmakerIds(bookmakerId)
                .sportIds(sportIds);

        client.subscribe(subscribe);

        sleep(1_000_000_000);
    }

    private String getBookmakerEventIdName(BookmakerEventDto bkEvent) {
        return getBookmakerEventIdName(bkEvent.name, bkEvent.startedAt);
    }

    private String getBookmakerEventIdName(BookmakerEventState bkEvent) {
        return getBookmakerEventIdName(bkEvent.getName(), bkEvent.getStartedAt());
    }

    private String getBookmakerEventIdName(String name, long startedAt) {
        return name + " [" + longToDateTimeWithMinutePrecisionWitoutYear(startedAt * 1000) + "]";
    }

    static public String longToDateTimeWithMinutePrecisionWitoutYear(long datetime) {
        return matchStartTimeFormat.format(new Date(datetime));
    }

    private String generateOutcomeName(OutcomeKey outcomeKey, boolean swapTeams, short sportId) {
        String periodName = Period.periodName(outcomeKey.getPeriodIdentifier(), sportId, true);
        return outcomeNameTranslator.translate(
                outcomeKey.getMarketAndBetTypeId(),
                outcomeKey.getMarketAndBetTypeParam(),
                outcomeKey.isLay(),
                swapTeams
        ).getEffectiveName() + " [" + periodName + "]" + (outcomeKey.isLay() ? " [LAY]" : "");
    }

    private static void printToConsole(String msg) {
        System.out.println(msg);
    }
}
