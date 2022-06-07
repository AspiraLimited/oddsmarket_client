package com.aspiralimited.oddsmarket.client.models;

import java.util.List;

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
                    this.id = ((value instanceof String) ? (String) value : value.toString());
                    break;

                case "bookmakerEventId":
                    this.bookmakerEventId = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "periodId":
                    this.periodId = (Integer) value;
                    break;

                case "periodIdentifier":
                    this.periodIdentifier = (Integer) value;
                    break;

                case "periodName":
                    this.periodName = (String) value;
                    break;

                case "betCombinationId":
                    this.betCombinationId = (Integer) value;
                    break;

                case "betCombination":
                    this.betCombination = (String) value;
                    break;

                case "marketAndBetTypeId":
                    this.marketAndBetTypeId = (short) (int) value;
                    break;

                case "marketAndBetTypeTitle":
                    this.marketAndBetTypeTitle = (String) value;
                    break;

                case "marketAndBetTypeParameterValue":
                    this.marketAndBetTypeParameterValue = ((Double) value).floatValue();
                    break;

                case "playerId1":
                    this.playerId1 = (Integer) value;
                    break;

                case "playerName1":
                    this.playerName1 = (String) value;
                    break;

                case "playerId2":
                    this.playerId2 = (Integer) value;
                    break;

                case "playerName2":
                    this.playerName2 = (String) value;
                    break;

                case "odd":
                    this.odd = ((Double) value).floatValue();
                    break;

                case "oddLay":
                    this.oddLay = ((Double) value).floatValue();
                    break;

                case "marketDepth":
                    this.marketDepth = ((Double) value).floatValue();
                    break;

                case "directLink":
                    this.directLink = (String) value;
                    break;

                case "updatedAt":
                    this.updatedAt = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "active":
                    this.active = (boolean) value;
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
