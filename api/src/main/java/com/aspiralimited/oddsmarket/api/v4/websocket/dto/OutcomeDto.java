package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import java.util.List;

public class OutcomeDto {

    public String id;
    public Long bookmakerEventId;
    public Integer periodId;
    public Integer periodIdentifier;
    public String periodName;
    public Short marketAndBetTypeId;
    public Float marketAndBetTypeParameterValue;
    public Integer playerId1;
    public String playerName1;
    public Integer playerId2;
    public String playerName2;
    public boolean active;
    public Float odds;
    public Float oddsLay = null;
    public boolean isLay;
    public Float marketDepth = null;
    public String directLink = null;
    public Long updatedAt;

    public OutcomeDto(List<Object> values, List<String> fields) {
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

                case "marketAndBetTypeId":
                    this.marketAndBetTypeId = (short) (int) value;
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

                case "active":
                    this.active = (boolean) value;
                    break;

                case "odds":
                    this.odds = ((Double) value).floatValue();
                    break;

                case "oddsLay":
                    this.oddsLay = ((Double) value).floatValue();
                    break;

                case "isLay":
                    this.isLay = (boolean) value;
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
        return "Outcome{" +
                "id=" + id +
                ", bookmakerEventId=" + bookmakerEventId +
                ", periodId=" + periodId +
                ", periodIdentifier=" + periodIdentifier +
                ", periodName='" + periodName + '\'' +
                ", marketAndBetTypeId=" + marketAndBetTypeId +
                ", marketAndBetTypeParameterValue=" + marketAndBetTypeParameterValue +
                ", playerId1=" + playerId1 +
                ", playerName1='" + playerName1 + '\'' +
                ", playerId2=" + playerId2 +
                ", playerName2='" + playerName2 + '\'' +
                ", active=" + active +
                ", odds=" + odds +
                ", oddsLay=" + oddsLay +
                ", isLay=" + isLay +
                ", marketDepth=" + marketDepth +
                ", directLink='" + directLink + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
