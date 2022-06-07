package com.aspiralimited.oddsmarket.client.demo.feedreader;

import com.aspiralimited.config.statical.Period;
import com.aspiralimited.oddsmarket.client.OddsmarketClient;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.DiffList;
import com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator.BetSpecCache;
import com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator.OutcomeNameTranslator;
import com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator.ReverseBetSpecCalculator;
import com.aspiralimited.oddsmarket.client.demo.feedreader.state.BookmakerEventState;
import com.aspiralimited.oddsmarket.client.demo.feedreader.state.InMemoryStateStorage;
import com.aspiralimited.oddsmarket.client.demo.feedreader.state.OutcomeData;
import com.aspiralimited.oddsmarket.client.demo.feedreader.state.OutcomeKey;
import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;
import com.aspiralimited.oddsmarket.client.models.Odd;
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

//        client.onJsonMessage(jsonMsg -> {
//            System.out.println("Response message: " + jsonMsg.optString("cmd") + " " + jsonMsg.opt("msg"));
//        });

        OddsmarketClient.Handler handler = new OddsmarketClient.Handler() {

            @Override
            public void info(String msg) {
                printToConsole("Info: " + msg);
            }

            @Override
            public void bookmakerEvent(BookmakerEvent bkEvent) {
                long bookmakerEventId = bkEvent.id;
                String bookmakerEventIdName = getBookmakerEventIdName(bkEvent);
                if (inMemoryStateStorage.hasBookmakerEvent(bookmakerEventId)) {
                    BookmakerEventState bookmakerEventState = inMemoryStateStorage.getBookmakerEvent(bookmakerEventId);
                    DiffList diffList = bookmakerEventState.updatePropertiesAndReturnDiff(bkEvent);
                    if (!diffList.isEmpty()) {
                        printToConsole("[UPD] " + bookmakerEventIdName + ": " + diffList.toString());
                    }
                } else {
                    BookmakerEventState bookmakerEventState = inMemoryStateStorage.putBookmakerEvent(bkEvent);
                    printToConsole("[NEW] " + getBookmakerEventIdName(bkEvent) + " " + bkEvent);
                }
            }

            @Override
            public void odds(Map<String, Odd> updatedOdds) {
                Map<Long, List<Odd>> oddsByBkEventId = new HashMap<>();
                for (Odd odd : updatedOdds.values()) {
                    oddsByBkEventId
                            .computeIfAbsent(odd.bookmakerEventId, id -> new ArrayList<>())
                            .add(odd);
                }
                for (Map.Entry<Long, List<Odd>> bkEventIdAndOdds : oddsByBkEventId.entrySet()) {
                    long bookmakerEventId = bkEventIdAndOdds.getKey();
                    if (inMemoryStateStorage.hasBookmakerEvent(bookmakerEventId)) {
                        BookmakerEventState bookmakerEventState = inMemoryStateStorage.getBookmakerEvent(bookmakerEventId);
                        String bookmakerEventIdName = getBookmakerEventIdName(bookmakerEventState);
                        printToConsole("[ODDS] " + bookmakerEventIdName);
                        for (Odd odd : bkEventIdAndOdds.getValue()) {
                            OutcomeKey outcomeKey = new OutcomeKey(odd.marketAndBetTypeId,
                                    odd.marketAndBetTypeParameterValue,
                                    (short) (int) odd.periodIdentifier,
                                    odd.oddLay != null && odd.oddLay != 0,
                                    odd.playerId1,
                                    odd.playerId2
                            );
                            String outcomeName = generateOutcomeName(outcomeKey, bookmakerEventState.isSwapTeams(), (short) bookmakerEventState.getSportId());
                            if (bookmakerEventState.hasOutcome(outcomeKey)) {
                                if (odd.active()) {
                                    DiffList diffList = bookmakerEventState.getOutcome(outcomeKey).updatePropertiesAndReturnDiff(odd);
                                    if (!diffList.isEmpty()) {
                                        printToConsole("    [UPD] " + outcomeName + ": " + diffList);
                                    }
                                    bookmakerEventState.putOutcome(outcomeKey, odd);
                                } else {
                                    // Deactivated outcomes must be removed
                                    bookmakerEventState.removeOutcome(outcomeKey);
                                    printToConsole("    [DEL] " + outcomeName);
                                }
                            } else {
                                if (odd.active()) {
                                    OutcomeData newOutcomeData = bookmakerEventState.putOutcome(outcomeKey, odd);
                                    printToConsole("    [NEW] " + outcomeName + ": " + newOutcomeData.toShortString());
                                } else {
                                    throw new IllegalStateException("Missing odd is being deactivated. ID=" + odd.id);
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

    private String getBookmakerEventIdName(BookmakerEvent bkEvent) {
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
