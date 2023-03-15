package com.aspiralimited.oddsmarket.api.v4.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class InternalEventDto {
    public long id;
    public String name;
    public Integer homeId;
    public Integer awayId;
    public short sportId;
    public Long leagueId;
    public long startDatetime;
}
