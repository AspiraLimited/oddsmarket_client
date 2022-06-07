package com.aspiralimited.oddsmarket.client.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class BetTypeDto {
    public short id;
    public Boolean changeSign;
    public String name;
    public Short reverseId;
    public String description;
}
