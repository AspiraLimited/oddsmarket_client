package com.aspiralimited.oddsmarket.api.v4.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CountryDto {
    public int id;
    public String name;
    public String code;
    public String iso3Code;
    public String slug;
}
