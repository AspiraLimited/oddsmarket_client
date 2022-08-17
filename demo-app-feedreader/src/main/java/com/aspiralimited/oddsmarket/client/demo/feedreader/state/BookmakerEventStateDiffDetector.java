package com.aspiralimited.oddsmarket.client.demo.feedreader.state;

import com.aspiralimited.oddsmarket.client.demo.feedreader.DiffPrinter;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.Diff;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.DiffList;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model.BookmakerEventState;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.BookmakerEventDto;
import lombok.Getter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


@Getter
public class BookmakerEventStateDiffDetector {
    private final static DateFormat preciseTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

    public static DiffList getStateVsDtoDiff(BookmakerEventState state, BookmakerEventDto dto) {
        DiffList diffList = new DiffList();
        if (!Objects.equals(state.getBookmakerId(), dto.bookmakerId)) {
            diffList.addDiff(new Diff("bookmakerId", state.getBookmakerId(), (short) dto.bookmakerId));
        }
        if (!Objects.equals(state.isActive(), dto.active)) {
            diffList.addDiff(new Diff("active", state.isActive(), dto.active));
        }
        if (!Objects.equals(state.getEventId(), dto.eventId)) {
            diffList.addDiff(new Diff("eventId", state.getEventId(), dto.eventId));
        }
        if (!Objects.equals(state.getName(), dto.name)) {
            diffList.addDiff(new Diff("name", state.getName(), dto.name));
        }
        if (!Objects.equals(state.getNameRu(), dto.nameRu)) {
            diffList.addDiff(new Diff("nameRu", state.getNameRu(), dto.nameRu));
        }
        if (!Objects.equals(state.isSwapTeams(), dto.swapTeams)) {
            diffList.addDiff(new Diff("swapTeams", state.isSwapTeams(), dto.swapTeams));
        }
        if (!Objects.equals(state.getCurrentScore(), dto.currentScore)) {
            diffList.addDiff(new Diff("currentScore", state.getCurrentScore(), dto.currentScore));
        }
        if (!Objects.equals(state.getStartedAt(), dto.startedAt)) {
            diffList.addDiff(new Diff("startedAt", DiffPrinter.longToDateTimeWithMinutePrecisionWitoutYear(state.getStartedAt() * 1000L), DiffPrinter.longToDateTimeWithMinutePrecisionWitoutYear(dto.startedAt * 1000L)));
        }
        if (!Objects.equals(state.getSportId(), dto.sportId)) {
            diffList.addDiff(new Diff("sportId", state.getSportId(), dto.sportId));
        }
        if (!Objects.equals(state.getLeagueId(), dto.leagueId)) {
            diffList.addDiff(new Diff("leagueId", state.getLeagueId(), dto.leagueId));
        }
        if (!Objects.equals(state.getRawId(), dto.rawId)) {
            diffList.addDiff(new Diff("rawId", state.getRawId(), dto.rawId));
        }
        if (!Objects.equals(state.getDirectLink(), dto.directLink)) {
            diffList.addDiff(new Diff("directLink", state.getDirectLink(), dto.directLink));
        }
        if (!Objects.equals(state.getUpdatedAt(), dto.updatedAt)) {
            diffList.addDiff(new Diff("updatedAt", updatedAtTimeFormat(state.getUpdatedAt() * 1000L), updatedAtTimeFormat(dto.updatedAt * 1000L)));
        }
        if (!Objects.equals(state.getHome(), dto.home)) {
            diffList.addDiff(new Diff("home", state.getHome(), dto.home));
        }
        if (!Objects.equals(state.getHomeId(), dto.homeId)) {
            diffList.addDiff(new Diff("homeId", state.getHomeId(), dto.homeId));
        }
        if (!Objects.equals(state.getAway(), dto.away)) {
            diffList.addDiff(new Diff("away", state.getAway(), dto.away));
        }
        if (!Objects.equals(state.getAwayId(), dto.awayId)) {
            diffList.addDiff(new Diff("awayId", state.getAwayId(), dto.awayId));
        }
        return diffList;
    }

    private static String updatedAtTimeFormat(Long datetime) {
        return preciseTimeFormat.format(new Date(datetime));
    }

}
