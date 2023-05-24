package com.aspiralimited.oddsmarket.client.models;

import java.util.List;

import static com.aspiralimited.oddsmarket.client.models.ValueReader.asBoolean;
import static com.aspiralimited.oddsmarket.client.models.ValueReader.asInt;
import static com.aspiralimited.oddsmarket.client.models.ValueReader.asLong;

public class BookmakerEvent {

    public Long id;
    public int bookmakerId;
    public boolean active;
    public Long eventId;
    public String name;
    public String nameRu;
    public boolean swapTeams;
    public String currentScore; // only for live
    public Long startedAt;
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

    public BookmakerEvent(List<Object> values, List<String> fields) {
        for (int i = 0; i < values.size(); i++) {
            String field = fields.get(i);
            Object value = values.get(i);

            if (value == null) continue;

            switch (field) {
                case "id":
                    this.id = asLong(value);
                    break;

                case "bookmakerId":
                    this.bookmakerId = asInt(value);
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
        return "BookmakerEvent{" +
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
