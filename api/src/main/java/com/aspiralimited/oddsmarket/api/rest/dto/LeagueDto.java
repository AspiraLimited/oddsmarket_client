package com.aspiralimited.oddsmarket.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class LeagueDto {
    public long id;
    public String name;
    public short sportId;
    public String countryIso3Code;
}
