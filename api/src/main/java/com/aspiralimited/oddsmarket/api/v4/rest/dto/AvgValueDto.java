package com.aspiralimited.oddsmarket.api.v4.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class AvgValueDto {

    public Status status;
    public Float odds;

    public enum Status {
        SUCCESS,
        FAILURE,
        INVALID_BOOKMAKER_ID,
        EVENT_ID_NOT_FOUND,
        BOOKMAKER_HAS_NO_BOOKMAKER_EVENTS_LINKED_TO_EVENT_ID,
        OUTCOME_NOT_FOUND,
    }
}
