package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asBoolean;
import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asFloat;
import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asInt;
import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asLong;
import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asShort;

@AllArgsConstructor
@NoArgsConstructor
public class OutcomeDto {

    public String id;
    public Long bookmakerEventId;
    public Short periodIdentifier;
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
                    this.id = value.toString();
                    break;

                case "bookmakerEventId":
                    this.bookmakerEventId = asLong(value);
                    break;

                case "periodIdentifier":
                    this.periodIdentifier = asShort(value);
                    break;

                case "periodName":
                    this.periodName = (String) value;
                    break;

                case "marketAndBetTypeId":
                    this.marketAndBetTypeId = asShort(value);
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

                case "active":
                    this.active = asBoolean(value);
                    break;

                case "odds":
                    this.odds = asFloat(value);
                    break;

                case "oddsLay":
                    this.oddsLay = asFloat(value);
                    break;

                case "isLay":
                    this.isLay = asBoolean(value);
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
