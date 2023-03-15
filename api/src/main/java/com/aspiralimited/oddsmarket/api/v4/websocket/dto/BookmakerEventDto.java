package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

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
                    this.id = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "bookmakerId":
                    this.bookmakerId = (short) (int) value;
                    break;

                case "active":
                    this.active = (boolean) value;
                    break;

                case "eventId":
                    this.eventId = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "name":
                    this.name = (String) value;
                    break;

                case "nameRu":
                    this.nameRu = (String) value;
                    break;

                case "swapTeams":
                    this.swapTeams = (boolean) value;
                    break;

                case "currentScore":
                    this.currentScore = (String) value;
                    break;

                case "startedAt":
                    this.startedAt = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "sportId":
                    this.sportId = (int) value;
                    break;

                case "sportName":
                    this.sportName = (String) value;
                    break;

                case "leagueId":
                    this.leagueId = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "leagueName":
                    this.leagueName = (String) value;
                    break;

                case "rawId":
                    this.rawId = (String) value;
                    break;

                case "directLink":
                    this.directLink = (String) value;
                    break;

                case "updatedAt":
                    this.updatedAt = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "homeId":
                    this.homeId = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "awayId":
                    this.awayId = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "home":
                    this.home = (String) value;
                    break;

                case "away":
                    this.away = (String) value;
                    break;

                case "eventHomeId":
                    this.eventHomeId = (int) value;
                    break;

                case "eventAwayId":
                    this.eventAwayId = (int) value;
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
