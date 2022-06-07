package com.aspiralimited.oddsmarket.client.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class MarketAndBetTypeDto {

    public short id;
    public String title;
    public short marketId;
    public short betTypeId;
    public Integer swapId;
}
