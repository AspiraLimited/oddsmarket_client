package com.aspiralimited.oddsmarket.client.demo.feedreader.state;

import com.aspiralimited.oddsmarket.client.demo.feedreader.DiffPrinter;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.Diff;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.DiffList;
import com.aspiralimited.oddsmarket.client.models.BookmakerEvent;
import com.aspiralimited.oddsmarket.client.models.Odd;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
@Getter
public class BookmakerEventState {
    private final static DateFormat preciseTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

    private final long id;
    private short bookmakerId;
    private boolean active;
    private Long eventId;
    private String name;
    private String nameRu;
    private boolean swapTeams;
    private String currentScore;
    private long startedAt;
    private int sportId;
    private Long leagueId;
    private String rawId;
    private String directLink;
    private long updatedAt;
    private Long homeId;
    private Long awayId;
    private String home;
    private String away;


    private Map<OutcomeKey, OutcomeData> outcomes = new ConcurrentHashMap<>();

    public DiffList updatePropertiesAndReturnDiff(BookmakerEvent bkEvent) {
        DiffList diffList = new DiffList();
        if (!Objects.equals(bookmakerId, bkEvent.bookmakerId)) {
            diffList.addDiff(new Diff("bookmakerId", bookmakerId, (short) bkEvent.bookmakerId));
            bookmakerId = (short) bkEvent.bookmakerId;
        }
        if (!Objects.equals(active, bkEvent.active)) {
            diffList.addDiff(new Diff("active", active, bkEvent.active));
            active = bkEvent.active;
        }
        if (!Objects.equals(eventId, bkEvent.eventId)) {
            diffList.addDiff(new Diff("eventId", eventId, bkEvent.eventId));
            eventId = bkEvent.eventId;
        }
        if (!Objects.equals(name, bkEvent.name)) {
            diffList.addDiff(new Diff("name", name, bkEvent.name));
            name = bkEvent.name;
        }
        if (!Objects.equals(nameRu, bkEvent.nameRu)) {
            diffList.addDiff(new Diff("nameRu", nameRu, bkEvent.nameRu));
            nameRu = bkEvent.nameRu;
        }
        if (!Objects.equals(swapTeams, bkEvent.swapTeams)) {
            diffList.addDiff(new Diff("swapTeams", swapTeams, bkEvent.swapTeams));
            swapTeams = bkEvent.swapTeams;
        }
        if (!Objects.equals(currentScore, bkEvent.currentScore)) {
            diffList.addDiff(new Diff("currentScore", currentScore, bkEvent.currentScore));
            currentScore = bkEvent.currentScore;
        }
        if (!Objects.equals(startedAt, bkEvent.startedAt)) {
            diffList.addDiff(new Diff("startedAt", DiffPrinter.longToDateTimeWithMinutePrecisionWitoutYear(startedAt * 1000), DiffPrinter.longToDateTimeWithMinutePrecisionWitoutYear(bkEvent.startedAt * 1000)));
            startedAt = bkEvent.startedAt;
        }
        if (!Objects.equals(sportId, bkEvent.sportId)) {
            diffList.addDiff(new Diff("sportId", sportId, bkEvent.sportId));
            sportId = bkEvent.sportId;
        }
        if (!Objects.equals(leagueId, bkEvent.leagueId)) {
            diffList.addDiff(new Diff("leagueId", leagueId, bkEvent.leagueId));
            leagueId = bkEvent.leagueId;
        }
        if (!Objects.equals(rawId, bkEvent.rawId)) {
            diffList.addDiff(new Diff("rawId", rawId, bkEvent.rawId));
            rawId = bkEvent.rawId;
        }
        if (!Objects.equals(directLink, bkEvent.directLink)) {
            diffList.addDiff(new Diff("directLink", directLink, bkEvent.directLink));
            directLink = bkEvent.directLink;
        }
        if (!Objects.equals(updatedAt, bkEvent.updatedAt)) {
            diffList.addDiff(new Diff("updatedAt", updatedAtTimeFormat(updatedAt * 1000), updatedAtTimeFormat(bkEvent.updatedAt * 1000)));
            updatedAt = bkEvent.updatedAt;
        }
        if (!Objects.equals(home, bkEvent.home)) {
            diffList.addDiff(new Diff("home", home, bkEvent.home));
            home = bkEvent.home;
        }
        if (!Objects.equals(homeId, bkEvent.homeId)) {
            diffList.addDiff(new Diff("homeId", homeId, bkEvent.homeId));
            homeId = bkEvent.homeId;
        }
        if (!Objects.equals(away, bkEvent.away)) {
            diffList.addDiff(new Diff("away", away, bkEvent.away));
            away = bkEvent.away;
        }
        if (!Objects.equals(awayId, bkEvent.awayId)) {
            diffList.addDiff(new Diff("awayId", awayId, bkEvent.awayId));
            awayId = bkEvent.awayId;
        }
        return diffList;
    }

    private static String updatedAtTimeFormat(Long datetime) {
        return preciseTimeFormat.format(new Date(datetime));
    }

    public boolean hasOutcome(OutcomeKey outcomeKey) {
        return outcomes.containsKey(outcomeKey);
    }

    public OutcomeData getOutcome(OutcomeKey outcomeKey) {
        return outcomes.get(outcomeKey);
    }

    public OutcomeData putOutcome(OutcomeKey outcomeKey, Odd odd) {
        OutcomeData newOutcomeData = new OutcomeData(odd.id,
                odd.bookmakerEventId,
                (short) (int) odd.periodIdentifier,
                odd.periodName,
                odd.marketAndBetTypeId,
                odd.marketAndBetTypeParameterValue,
                odd.playerId1,
                odd.playerId2
        );
        newOutcomeData.updatePropertiesAndReturnDiff(odd);
        outcomes.put(outcomeKey, newOutcomeData);
        return newOutcomeData;
    }

    public void removeOutcome(OutcomeKey outcomeKey) {
        outcomes.remove(outcomeKey);
    }
}
