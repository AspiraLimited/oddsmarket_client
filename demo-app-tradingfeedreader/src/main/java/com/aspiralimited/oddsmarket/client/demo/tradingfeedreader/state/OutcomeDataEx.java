package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.state;

import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.diff.Diff;
import com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.diff.DiffList;
import com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model.OutcomeData;
import com.aspiralimited.oddsmarket.api.v4.websocket.dto.OutcomeDto;

import java.util.Objects;

public class OutcomeDataEx extends OutcomeData {

    public OutcomeDataEx(String id, long bookmakerEventId, short periodIdentifier, String periodName, short marketAndBetTypeId, float marketAndBetTypeParameterValue, Integer playerId1, Integer playerId2) {
        super(id, bookmakerEventId, periodIdentifier, periodName, marketAndBetTypeId, marketAndBetTypeParameterValue, playerId1, playerId2);
    }

    public DiffList updatePropertiesAndReturnDiff(OutcomeDto outcomeDto) {
        DiffList diffList = new DiffList();
        if (!Objects.equals(getPlayerName1(), outcomeDto.playerName1)) {
            diffList.addDiff(new Diff("playerName1", getPlayerName1(), outcomeDto.playerName1));
        }
        if (!Objects.equals(getPlayerName2(), outcomeDto.playerName2)) {
            diffList.addDiff(new Diff("playerName2", getPlayerName2(), outcomeDto.playerName2));
        }
        if (!Objects.equals(getOdds(), outcomeDto.odds)) {
            diffList.addDiff(new Diff("odds", getOdds(), outcomeDto.odds));
        }
        if (!Objects.equals(getOddsLay(), outcomeDto.oddsLay)) {
            diffList.addDiff(new Diff("oddsLay", getOddsLay(), outcomeDto.oddsLay));
        }
        if (!Objects.equals(getMarketDepth(), outcomeDto.marketDepth)) {
            diffList.addDiff(new Diff("marketDepth", getMarketDepth(), outcomeDto.marketDepth));
        }
        if (!Objects.equals(getDirectLink(), outcomeDto.directLink)) {
            diffList.addDiff(new Diff("directLink", getDirectLink(), outcomeDto.directLink));
        }

        updateProperties(outcomeDto);

        return diffList;
    }

    public String toShortString() {
        return "odds=" + getOdds()
                + (getOddsLay() != null && getOddsLay() > 0 ? ", oddsLay=" + getOddsLay() : "")
                + (getMarketDepth() != null && getMarketDepth() > 0 ? ", marketDepth=" + getMarketDepth() : "")
                + ", directLink=" + getDirectLink();
    }
}
