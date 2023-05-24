package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asBoolean;
import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asInt;
import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asLong;
import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asShort;

@AllArgsConstructor
@NoArgsConstructor
public class BookmakerEventDto {

    public long id;
    public short bookmakerId;
    public boolean active;
    public Long eventId;
    public String name;
    public String nameRu;
    public boolean swapTeams;
    public String currentScore; // only for live
    public long startedAt;
    public int sportId;
    public String sportName;
    public Long leagueId;
    public String leagueName;
    public String rawId;
    public String directLink;
    public Long updatedAt;
    public Long homeId;
    public Long awayId;
    public String home;
    public String away;
    public Integer eventHomeId;
    public Integer eventAwayId;

    public BookmakerEventDto(List<Object> values, List<String> fields) {
        for (int i = 0; i < values.size(); i++) {
            String field = fields.get(i);
            Object value = values.get(i);

            if (value == null) continue;

            switch (field) {
                case "id":
                    this.id = asLong(value);
                    break;

                case "bookmakerId":
                    this.bookmakerId = asShort(value);
                    break;

                case "active":
                    this.active = asBoolean(value);
                    break;

                case "eventId":
                    this.eventId = asLong(value);
                    break;

                case "name":
                    this.name = value.toString();
                    break;

                case "nameRu":
                    this.nameRu = value.toString();
                    break;

                case "swapTeams":
                    this.swapTeams = asBoolean(value);
                    break;

                case "currentScore":
                    this.currentScore = value.toString();
                    break;

                case "startedAt":
                    this.startedAt = asLong(value);
                    break;

                case "sportId":
                    this.sportId = asInt(value);
                    break;

                case "sportName":
                    this.sportName = value.toString();
                    break;

                case "leagueId":
                    this.leagueId = asLong(value);
                    break;

                case "leagueName":
                    this.leagueName = value.toString();
                    break;

                case "rawId":
                    this.rawId = value.toString();
                    break;

                case "directLink":
                    this.directLink = value.toString();
                    break;

                case "updatedAt":
                    this.updatedAt = asLong(value);
                    break;

                case "homeId":
                    this.homeId = asLong(value);
                    break;

                case "awayId":
                    this.awayId = asLong(value);
                    break;

                case "home":
                    this.home = value.toString();
                    break;

                case "away":
                    this.away = value.toString();
                    break;

                case "eventHomeId":
                    this.eventHomeId = asInt(value);
                    break;

                case "eventAwayId":
                    this.eventAwayId = asInt(value);
                    break;

                default:
                    System.out.println("unknown field: " + field + "[" + value + "]");
            }
        }
    }

    @Override
    public String toString() {
        return "BookmakerEventDto{" +
                "id=" + id +
                ", bookmakerId=" + bookmakerId +
                ", active=" + active +
                ", eventId=" + eventId +
                ", name='" + name + '\'' +
                ", nameRu='" + nameRu + '\'' +
                ", home='" + home + '\'' +
                ", away='" + away + '\'' +
                ", homeId='" + homeId + '\'' +
                ", awayId='" + awayId + '\'' +
                ", eventHomeId='" + eventHomeId + '\'' +
                ", eventAwayId='" + eventAwayId + '\'' +
                ", swapTeams=" + swapTeams +
                ", currentScore='" + currentScore + '\'' +
                ", startedAt=" + startedAt +
                ", sportId=" + sportId +
                ", sportName='" + sportName + '\'' +
                ", leagueId='" + leagueId + '\'' +
                ", leagueName='" + leagueName + '\'' +
                ", rawId='" + rawId + '\'' +
                ", directLink='" + directLink + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
