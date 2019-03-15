package com.aspiralimited.oddsmarket.client.models;

import java.util.List;

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
                    this.id = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "bookmakerId":
                    this.bookmakerId = (int) value;
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
                ", leagueName='" + leagueName + '\'' +
                ", rawId='" + rawId + '\'' +
                ", directLink='" + directLink + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
