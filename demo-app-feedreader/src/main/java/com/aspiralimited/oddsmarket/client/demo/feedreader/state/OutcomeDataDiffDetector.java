package com.aspiralimited.oddsmarket.client.demo.feedreader.state;

import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.Diff;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.DiffList;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model.OutcomeData;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model.OutcomeKey;

import java.util.Objects;

public class OutcomeDataDiffDetector {


    public static DiffList getDiffList(OutcomeData state, OutcomeDto dto) {
        DiffList diffList = new DiffList();
        if (!Objects.equals(state.getPlayerName1(), dto.playerName1)) {
            diffList.addDiff(new Diff("playerName1", state.getPlayerName1(), dto.playerName1));
        }
        if (!Objects.equals(state.getPlayerName2(), dto.playerName2)) {
            diffList.addDiff(new Diff("playerName2", state.getPlayerName2(), dto.playerName2));
        }
        if (!Objects.equals(state.getOdds(), dto.odds)) {
            diffList.addDiff(new Diff("odds", state.getOdds(), dto.odds));
        }
        if (!Objects.equals(state.getOddsLay(), dto.oddsLay)) {
            diffList.addDiff(new Diff("oddsLay", state.getOddsLay(), dto.oddsLay));
        }
        if (!Objects.equals(state.getMarketDepth(), dto.marketDepth)) {
            diffList.addDiff(new Diff("marketDepth", state.getMarketDepth(), dto.marketDepth));
        }
        if (!Objects.equals(state.getDirectLink(), dto.directLink)) {
            diffList.addDiff(new Diff("directLink", state.getDirectLink(), dto.directLink));
        }
        return diffList;
    }

    public static String outcomeDtoToShortString(OutcomeDto outcomeDto) {
        return "odds=" + outcomeDto.odds
                + (outcomeDto.isLay ? ", oddsLay=" + outcomeDto.oddsLay : "")
                + (outcomeDto.marketDepth != null && outcomeDto.marketDepth > 0 ? ", marketDepth=" + outcomeDto.marketDepth : "")
                + ", directLink=" + outcomeDto.directLink;
    }
}
