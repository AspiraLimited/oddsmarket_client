package com.aspiralimited.oddsmarket.client.models;

import java.util.List;

import static com.aspiralimited.oddsmarket.client.models.ValueReader.asBoolean;
import static com.aspiralimited.oddsmarket.client.models.ValueReader.asFloat;
import static com.aspiralimited.oddsmarket.client.models.ValueReader.asInt;
import static com.aspiralimited.oddsmarket.client.models.ValueReader.asLong;
import static com.aspiralimited.oddsmarket.client.models.ValueReader.asShort;

public class Odd {

    public String id;
    public Long bookmakerEventId;
    public Integer periodId;
    public Integer periodIdentifier;
    public String periodName;
    public Integer betCombinationId;
    public String betCombination;
    public Short marketAndBetTypeId;
    public String marketAndBetTypeTitle;
    public Float marketAndBetTypeParameterValue;
    public Integer playerId1;
    public String playerName1;
    public Integer playerId2;
    public String playerName2;
    public boolean active;
    public Float odd;
    public Float oddLay = null;
    public Float marketDepth = null;
    public String directLink = null;
    public Long updatedAt;

    public Odd(List<Object> values, List<String> fields) {
        for (int i = 0; i < values.size(); i++) {
            String field = fields.get(i);
            Object value = values.get(i);

            if (value == null) continue;

            switch (field) {
                case "id":
                    this.id = value.toString();
                    break;

                case "bookmakerEventId":
                    this.bookmakerEventId = asLong(value);
                    break;

                case "periodId":
                    this.periodId = asInt(value);
                    break;

                case "periodIdentifier":
                    this.periodIdentifier = asInt(value);
                    break;

                case "periodName":
                    this.periodName = value.toString();
                    break;

                case "betCombinationId":
                    this.betCombinationId = asInt(value);
                    break;

                case "betCombination":
                    this.betCombination = value.toString();
                    break;

                case "marketAndBetTypeId":
                    this.marketAndBetTypeId = asShort(value);
                    break;

                case "marketAndBetTypeTitle":
                    this.marketAndBetTypeTitle = value.toString();
                    break;

                case "marketAndBetTypeParameterValue":
                    this.marketAndBetTypeParameterValue = asFloat(value);
                    break;

                case "playerId1":
                    this.playerId1 = asInt(value);
                    break;

                case "playerName1":
                    this.playerName1 = value.toString();
                    break;

                case "playerId2":
                    this.playerId2 = asInt(value);
                    break;

                case "playerName2":
                    this.playerName2 = value.toString();
                    break;

                case "odd":
                    this.odd = asFloat(value);
                    break;

                case "oddLay":
                    this.oddLay = asFloat(value);
                    break;

                case "marketDepth":
                    this.marketDepth = asFloat(value);
                    break;

                case "directLink":
                    this.directLink = value.toString();
                    break;

                case "updatedAt":
                    this.updatedAt = asLong(value);
                    break;

                case "active":
                    this.active = asBoolean(value);
                    break;

                default:
                    System.out.println("unknown field: " + field + "[" + value + "]");
            }
        }
    }

    public boolean active() {
        return active;
    }

    @Override
    public String toString() {
        return "Odd{" +
                "id=" + id +
                ", bookmakerEventId=" + bookmakerEventId +
                ", periodId=" + periodId +
                ", periodIdentifier=" + periodIdentifier +
                ", periodName='" + periodName + '\'' +
                ", betCombinationId=" + betCombinationId +
                ", betCombination='" + betCombination + '\'' +
                ", marketAndBetTypeId=" + marketAndBetTypeId +
                ", marketAndBetTypeTitle='" + marketAndBetTypeTitle + '\'' +
                ", marketAndBetTypeParameterValue=" + marketAndBetTypeParameterValue +
                ", playerId1=" + playerId1 +
                ", playerName1='" + playerName1 + '\'' +
                ", playerId2=" + playerId2 +
                ", playerName2='" + playerName2 + '\'' +
                ", active=" + active +
                ", odd=" + odd +
                ", oddLay=" + oddLay +
                ", marketDepth=" + marketDepth +
                ", directLink='" + directLink + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
