package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.diff;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Diff {
    private final String fieldName;
    private final String oldFieldValue;
    private final String newFieldValue;

    public Diff(String fieldName, Long oldFieldValue, Long newFieldValue) {
        this.fieldName = fieldName;
        this.oldFieldValue = toString(oldFieldValue);
        this.newFieldValue = toString(newFieldValue);
    }

    private String toString(Long value) {
        return value == null ? "null" : Long.toString(value);
    }

    public Diff(String fieldName, Boolean oldFieldValue, Boolean newFieldValue) {
        this.fieldName = fieldName;
        this.oldFieldValue = toString(oldFieldValue);
        this.newFieldValue = toString(newFieldValue);
    }

    private String toString(Boolean value) {
        return value == null ? "null" : Boolean.toString(value);
    }

    public Diff(String fieldName, Short oldFieldValue, Short newFieldValue) {
        this.fieldName = fieldName;
        this.oldFieldValue = toString(oldFieldValue);
        this.newFieldValue = toString(newFieldValue);
    }

    private String toString(Short value) {
        return value == null ? "null" : Short.toString(value);
    }


    public Diff(String fieldName, Integer oldFieldValue, Integer newFieldValue) {
        this.fieldName = fieldName;
        this.oldFieldValue = toString(oldFieldValue);
        this.newFieldValue = toString(newFieldValue);
    }

    private String toString(Integer value) {
        return value == null ? "null" : Integer.toString(value);
    }

    public Diff(String fieldName, Float oldFieldValue, Float newFieldValue) {
        this.fieldName = fieldName;
        this.oldFieldValue = toString(oldFieldValue);
        this.newFieldValue = toString(newFieldValue);
    }

    private String toString(Float value) {
        return value == null ? "null" : Float.toString(value);
    }

    @Override
    public String toString() {
        return fieldName + ": " + oldFieldValue + "->" + newFieldValue;
    }
}
