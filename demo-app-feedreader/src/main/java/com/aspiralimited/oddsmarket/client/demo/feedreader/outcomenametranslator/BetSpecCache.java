package com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BetSpecCache {
    final Map<Long, BetSpec> betSpecCache = new ConcurrentHashMap<>();

    public BetSpec getOrCreateBetSpec(short marketAndBetType, float marketAndBetTypeParam, short period, boolean isLay, int playerId1, int playerId2) {
        if (playerId1 == 0 && playerId2 == 0 && marketAndBetTypeParam < 100 && period < 100) {
            // Строим 64-битный ключ из значимых параметров BetSpec: marketAndBetType (15 bits), marketAndBetTypeParam (32 bits), period (16 bits), isLay (1 bit)
            long key = marketAndBetType;
            key = key << 32;
            key = key | (0xFFFFFFFFL & (long) Float.floatToRawIntBits(marketAndBetTypeParam));
            key = key << 16;
            key = key | period;
            key = key << 1;
            key = key | (isLay ? 1 : 0);

            return betSpecCache.computeIfAbsent(key, aLong -> new BetSpec(
                    marketAndBetType, marketAndBetTypeParam, period, isLay, playerId1, playerId2
            ));
        }
        return new BetSpec(
                marketAndBetType, marketAndBetTypeParam, period, isLay, playerId1, playerId2
        );
    }
}
