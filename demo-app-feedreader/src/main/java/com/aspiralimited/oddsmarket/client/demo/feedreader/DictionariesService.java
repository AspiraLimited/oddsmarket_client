package com.aspiralimited.oddsmarket.client.demo.feedreader;

import com.aspiralimited.oddsmarket.client.rest.OddsmarketRestHttpClient;
import com.aspiralimited.oddsmarket.client.rest.dto.BetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketAndBetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.SportDto;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DictionariesService {
    @Getter
    private final Map<Short, MarketAndBetTypeDto> marketAndBetTypeDtoById;
    @Getter
    private final Map<Short, BetTypeDto> betTypeDtoById;
    private final OddsmarketRestHttpClient oddsmarketRestHttpClient;
    private final Map<Short, SportDto> sportDtoById;

    @SneakyThrows
    public DictionariesService(OddsmarketRestHttpClient oddsmarketRestHttpClient) {
        this.oddsmarketRestHttpClient = oddsmarketRestHttpClient;

        List<MarketAndBetTypeDto> marketAndBetTypeDtos = oddsmarketRestHttpClient.getMarketAndBetTypes().get();
        marketAndBetTypeDtoById = marketAndBetTypeDtos.stream().collect(Collectors.toMap(x -> x.id, x -> x));

        List<BetTypeDto> betTypeDtos = oddsmarketRestHttpClient.getBetTypes().get();
        betTypeDtoById = betTypeDtos.stream().collect(Collectors.toMap(x -> x.id, x -> x));

        List<SportDto> sportDtos = oddsmarketRestHttpClient.getSports().get();
        sportDtoById = sportDtos.stream().collect(Collectors.toMap(x -> x.id, x -> x));
    }
}
