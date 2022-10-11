package com.aspiralimited.oddsmarket.api.v4.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataHealthDto {
    public int bookmakerEventsCount;
    public int bookmakerEventsWithActiveOutcomesCount;
    public int outcomesCount;
}
