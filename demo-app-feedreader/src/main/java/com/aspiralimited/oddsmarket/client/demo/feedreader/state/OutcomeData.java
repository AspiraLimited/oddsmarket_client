package com.aspiralimited.oddsmarket.client.demo.feedreader.state;

import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.Diff;
import com.aspiralimited.oddsmarket.client.demo.feedreader.diff.DiffList;
import com.aspiralimited.oddsmarket.client.models.Odd;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class OutcomeData {
    private final String id;
    private final long bookmakerEventId;
    private final short periodIdentifier;
    private final String periodName;
    private final short marketAndBetTypeId;
    private final float marketAndBetTypeParameterValue;
    private final Integer playerId1;
    private String playerName1;
    private final Integer playerId2;
    private String playerName2;
    private float odds;
    private Float oddsLay = null;
    private Float marketDepth = null;
    private String directLink = null;

    public DiffList updatePropertiesAndReturnDiff(Odd odd) {
        DiffList diffList = new DiffList();
        if (!Objects.equals(playerName1, odd.playerName1)) {
            diffList.addDiff(new Diff("playerName1", playerName1, odd.playerName1));
            playerName1 = odd.playerName1;
        }
        if (!Objects.equals(playerName2, odd.playerName2)) {
            diffList.addDiff(new Diff("playerName2", playerName2, odd.playerName2));
            playerName2 = odd.playerName2;
        }
        if (!Objects.equals(odds, odd.odd)) {
            diffList.addDiff(new Diff("odds", odds, odd.odd));
            odds = odd.odd;
        }
        if (!Objects.equals(oddsLay, odd.oddLay)) {
            diffList.addDiff(new Diff("oddsLay", oddsLay, odd.oddLay));
            oddsLay = odd.oddLay;
        }
        if (!Objects.equals(marketDepth, odd.marketDepth)) {
            diffList.addDiff(new Diff("marketDepth", marketDepth, odd.marketDepth));
            marketDepth = odd.marketDepth;
        }
        if (!Objects.equals(directLink, odd.directLink)) {
            diffList.addDiff(new Diff("directLink", directLink, odd.directLink));
            directLink = odd.directLink;
        }

        return diffList;
    }

    public String toShortString() {
        return "odds=" + odds
                + (oddsLay != null && oddsLay > 0 ? ", oddsLay=" + oddsLay : "")
                + (marketDepth != null && marketDepth > 0 ? ", marketDepth=" + marketDepth : "")
                + ", directLink=" + directLink;
    }
}
