package com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.dto.BookmakerEventDto;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Getter
public class BookmakerEventState {

    private final long id;
    private volatile short bookmakerId;
    private volatile boolean active;
    private volatile Long eventId;
    private volatile String name;
    private volatile String nameRu;
    private volatile boolean swapTeams;
    private volatile String currentScore;
    private volatile long startedAt;
    private volatile int sportId;
    private volatile Long leagueId;
    private volatile String rawId;
    private volatile String directLink;
    private volatile long updatedAt;
    private volatile Long homeId;
    private volatile Long awayId;
    private volatile String home;
    private volatile String away;

    private List<Player> players = new ArrayList<>();

    private Map<OutcomeKey, OutcomeData> outcomes = new ConcurrentHashMap<>();

    public void updateProperties(BookmakerEventDto bkEvent) {
        bookmakerId = (short) bkEvent.bookmakerId;
        active = bkEvent.active;
        eventId = bkEvent.eventId;
        name = bkEvent.name;
        nameRu = bkEvent.nameRu;
        swapTeams = bkEvent.swapTeams;
        currentScore = bkEvent.currentScore;
        startedAt = bkEvent.startedAt;
        sportId = bkEvent.sportId;
        leagueId = bkEvent.leagueId;
        rawId = bkEvent.rawId;
        directLink = bkEvent.directLink;
        updatedAt = bkEvent.updatedAt;
        home = bkEvent.home;
        homeId = bkEvent.homeId;
        away = bkEvent.away;
        awayId = bkEvent.awayId;
        players = bkEvent.players.stream().map(Player::of).collect(Collectors.toList());
    }


    public boolean hasOutcome(OutcomeKey outcomeKey) {
        return outcomes.containsKey(outcomeKey);
    }

    public OutcomeData getOutcome(OutcomeKey outcomeKey) {
        return outcomes.get(outcomeKey);
    }

    public OutcomeData putOutcome(OutcomeKey outcomeKey, OutcomeDto outcomeDto) {
        return outcomes.compute(outcomeKey, (outcomeKey1, outcomeData) -> {
            if (outcomeData == null) {
                outcomeData = new OutcomeData(outcomeDto.id,
                        outcomeDto.bookmakerEventId,
                        (short) (int) outcomeDto.periodIdentifier,
                        outcomeDto.periodName,
                        outcomeDto.marketAndBetTypeId,
                        outcomeDto.marketAndBetTypeParameterValue,
                        outcomeDto.playerId1,
                        outcomeDto.playerId2
                );
            }
            outcomeData.updateProperties(outcomeDto);
            return outcomeData;
        });
    }

    public void removeOutcome(OutcomeKey outcomeKey) {
        outcomes.remove(outcomeKey);
    }
}
