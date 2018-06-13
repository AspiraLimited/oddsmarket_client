package com.aspiralimited.oddsmarket.client.models;

import java.util.List;

public class Odd {

    public Long id;
    public Long bookmakerEventId;
    public Integer periodId;
    public Integer betCombinationId;
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
                    this.id = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "bookmakerEventId":
                    this.bookmakerEventId = ((value instanceof Integer) ? (long) (int) value : (long) value);
                    break;

                case "periodId":
                    this.periodId = (Integer) value;
                    break;

                case "betCombinationId":
                    this.betCombinationId = (Integer) value;
                    break;

                case "active":
                    this.active = (boolean) value;
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

                default:
                    System.out.println("unknown field: " + field + "[" + value + "]");
            }
        }
    }

    public boolean active() {
        return active;
    }
}
