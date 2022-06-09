package com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator;

import com.aspiralimited.config.statical.BetType;
import com.aspiralimited.config.statical.Market;
import com.aspiralimited.oddsmarket.client.rest.dto.BetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketAndBetTypeDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReverseBetSpecCalculator {
    public static final Set<Short> _1X2_BET_TYPES = Set.of(BetType._1.id, BetType._X.id, BetType._2.id);
    public static final Set<Short> _DC_BET_TYPES = Set.of(BetType._1X.id, BetType._X2.id, BetType._12.id);
    final Map<Short, MarketAndBetTypeDto> marketAndBetTypeDto;
    final Map<Short, BetTypeDto> betTypeConfigMap;
    final BetSpecCache betSpecCache;
    // Индекс MarketAndBetTypeDto по betTypeId, marketId
    final Map<Short, Map<Short, MarketAndBetTypeDto>> marketAndBetTypeDtoByBetTypeAndMarket;

    public ReverseBetSpecCalculator(Map<Short, MarketAndBetTypeDto> marketAndBetTypeDto, Map<Short, BetTypeDto> betTypeConfigMap, BetSpecCache betSpecCache) {
        this.marketAndBetTypeDto = marketAndBetTypeDto;
        this.betTypeConfigMap = betTypeConfigMap;
        this.betSpecCache = betSpecCache;

        marketAndBetTypeDtoByBetTypeAndMarket = new HashMap<>();
        for (MarketAndBetTypeDto marketAndBetTypeDtoValue : marketAndBetTypeDto.values()) {
            marketAndBetTypeDtoByBetTypeAndMarket
                    .computeIfAbsent(marketAndBetTypeDtoValue.betTypeId, betTypeId -> new HashMap<>())
                    .compute(marketAndBetTypeDtoValue.marketId, (aShort, marketAndBetTypeDtoOld) -> {
                        if (marketAndBetTypeDtoOld != null) {
                            throw new RuntimeException("Invalid configuration: duplicate marketAndBetType record for betTypeId=" + marketAndBetTypeDtoValue.betTypeId
                                    + ", marketId=" + marketAndBetTypeDtoValue.marketId);
                        }
                        return marketAndBetTypeDtoValue;
                    });
        }
    }

    public BetSpec constructReverseBetSpec(BetSpec betSpec) {
        MarketAndBetTypeDto sourceMarketAndBetType = marketAndBetTypeDto.get(betSpec.marketAndBetType);
        if (sourceMarketAndBetType == null) {
            throw new RuntimeException("Can't resolve betSpec.marketAndBetType=" + betSpec.marketAndBetType);
        }
        BetTypeDto betType = betTypeConfigMap.get(sourceMarketAndBetType.betTypeId);
        if (betType == null) {
            throw new RuntimeException("Can't resolve sourceMarketAndBetType.betTypeId=" + sourceMarketAndBetType.betTypeId);
        }
        Short reverseId = betType.reverseId;
        if (betType.id == BetType._EH1.id) {
            reverseId = BetType._EH2.id;
        } else if (betType.id == BetType._EH2.id) {
            reverseId = BetType._EH1.id;
        }
        if (reverseId != null) {
            short reverseMarketId;
            if (Market.REVERSE_MARKETS.containsKey(sourceMarketAndBetType.marketId)) {
                reverseMarketId = Market.REVERSE_MARKETS.get(sourceMarketAndBetType.marketId);
            } else if (Market.NO_REVERSE_MARKETS.contains(sourceMarketAndBetType.marketId)) {
                return null;
            } else {
                reverseMarketId = sourceMarketAndBetType.marketId;
            }
            MarketAndBetTypeDto reverseMarketAndBetTypeDto = marketAndBetTypeDtoByBetTypeAndMarket.get(reverseId).get(reverseMarketId);
            if (reverseMarketAndBetTypeDto == null) {
                throw new RuntimeException("No reverse MarketAndBetTypeDto for betSpec=" + betSpec + "; reverseId=" + reverseId + ", reverseMarketId=" + reverseMarketId);
            }
            float reverseParam = betSpec.marketAndBetTypeParam;
            if (betType.id == BetType._EH1.id || betType.id == BetType._EH2.id) {
                reverseParam = 1f - betSpec.marketAndBetTypeParam;
            } else if (betType.changeSign != null && betType.changeSign) {
                reverseParam = 0-betSpec.marketAndBetTypeParam;
            }
            return betSpecCache.getOrCreateBetSpec(reverseMarketAndBetTypeDto.id, reverseParam, betSpec.period, betSpec.isLay, betSpec.playerId1, betSpec.playerId2);
        } else {
            return null;
        }
    }

    /**
     * Возвращает два реверсных исхода для исходов с рынков 1X2.
     * В остальных случаях возвращает один или ноль реверсных исходов (как constructReverseBetSpec).
     */
    public Set<BetSpec> constructMultiReverseBetSpecs(BetSpec betSpec) {
        MarketAndBetTypeDto sourceMarketAndBetType = marketAndBetTypeDto.get(betSpec.marketAndBetType);
        if (sourceMarketAndBetType == null) {
            throw new RuntimeException("Can't resolve betSpec.marketAndBetType=" + betSpec.marketAndBetType);
        }
        BetTypeDto betType = betTypeConfigMap.get(sourceMarketAndBetType.betTypeId);
        if (betType == null) {
            throw new RuntimeException("Can't resolve sourceMarketAndBetType.betTypeId=" + sourceMarketAndBetType.betTypeId);
        }

        if (_1X2_BET_TYPES.contains(sourceMarketAndBetType.betTypeId)) {
            Set<Short> reverseBetIds = new HashSet<>(_1X2_BET_TYPES);
            reverseBetIds.remove(sourceMarketAndBetType.betTypeId);
            Set<BetSpec> result = new HashSet<>(2);
            for (Short reverseBetId : reverseBetIds) {
                MarketAndBetTypeDto reverseMarketAndBetTypeDto = marketAndBetTypeDtoByBetTypeAndMarket.get(reverseBetId).get(sourceMarketAndBetType.marketId);
                if (reverseMarketAndBetTypeDto == null) {
                    throw new RuntimeException("No marketAndBetType found for betId=" + reverseBetId + " and marketId=" + sourceMarketAndBetType.marketId);
                }
                result.add(
                        betSpecCache.getOrCreateBetSpec(reverseMarketAndBetTypeDto.id, betSpec.marketAndBetTypeParam, betSpec.period, betSpec.isLay, betSpec.playerId1, betSpec.playerId2)
                );
            }
            return result;
        } else {
            BetSpec singularResult = constructReverseBetSpec(betSpec);
            if (singularResult == null) {
                return Collections.emptySet();
            } else {
                return Set.of(singularResult);
            }
        }
    }

    public boolean isDoubleChanceMarket(short marketAndBetTypeId) {
        MarketAndBetTypeDto marketAndBetType = marketAndBetTypeDto.get(marketAndBetTypeId);
        if (marketAndBetType == null) {
            throw new RuntimeException("Can't resolve marketAndBetType=" + marketAndBetTypeId);
        }
        return _DC_BET_TYPES.contains(marketAndBetType.betTypeId);
    }
}
