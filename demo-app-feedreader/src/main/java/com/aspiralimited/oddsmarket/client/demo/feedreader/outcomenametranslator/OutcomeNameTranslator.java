package com.aspiralimited.oddsmarket.client.demo.feedreader.outcomenametranslator;

import com.aspiralimited.config.statical.BetType;
import com.aspiralimited.oddsmarket.api.rest.dto.MarketAndBetTypeDto;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.ResourceBundle;

@RequiredArgsConstructor
public class OutcomeNameTranslator {
    public static final String RESOURCE_BUNDLE_NAME = "MarketAndBetType";
    private final Map<Short, MarketAndBetTypeDto> marketAndBetTypeConfigMap;
    private final ResourceBundle resourceBundle;
    private final ReverseBetSpecCalculator reverseBetSpecCalculator;

    static public String formatSpreadParam(short betTypeId, float marketAndBetTypeParamValue) {
        if (marketAndBetTypeParamValue > 0 && (
                betTypeId == BetType._F1.id
                        || betTypeId == BetType._F2.id
                        || betTypeId == BetType._SET_F1.id
                        || betTypeId == BetType._SET_F2.id
        )) {
            return "+" + marketAndBetTypeParamValue;
        } else {
            return "" + marketAndBetTypeParamValue;
        }
    }

    static String formatExactGoalsValue(float marketAndBetTypeParamValue) {
        if (marketAndBetTypeParamValue == 0) {
            return "0";
        } else {
            String[] numbers = (marketAndBetTypeParamValue + "").split("\\.");
            String num1 = numbers[0];
            String num2 = numbers[1];
            if (num1.equals(num2)) {
                num2 = "";
            }
            String result = num1 + "-" + num2;
            return result.replaceAll("-0", "")
                    .replaceAll("-$", "+");
        }
    }

    /**
     * Translates outcome (marketAndBetTypeId, marketAndBetTypeParamValue) into human-readable format
     *
     * @param marketAndBetTypeId         - marketAndBetType id
     * @param marketAndBetTypeParamValue - float outcome parameter
     * @param isLay                      - outcome was created from lay price on betting exchange
     * @param swapTeams                  - outcome was transformed by swapTeams flag
     * @return
     */
    public OutcomeName translate(short marketAndBetTypeId, float marketAndBetTypeParamValue, boolean isLay, boolean swapTeams) {
        MarketAndBetTypeDto marketAndBetTypeConfig = marketAndBetTypeConfigMap.get(marketAndBetTypeId);
        if (marketAndBetTypeConfig == null) {
            throw new RuntimeException("MarketAndBetType not found by id=" + marketAndBetTypeId);
        }
        String title = marketAndBetTypeConfig.title;
        String translatedTemplate = resourceBundle.getString(title);

        OutcomeName result = null;
        if (swapTeams && marketAndBetTypeConfig.swapId != null
                && !marketAndBetTypeConfig.swapId.equals((int) marketAndBetTypeConfig.id)
        ) {
            MarketAndBetTypeDto swapMarketAndBetTypeConfig = marketAndBetTypeConfigMap.get((short) (int) marketAndBetTypeConfig.swapId);
            if (swapMarketAndBetTypeConfig == null) {
                throw new RuntimeException("MarketAndBetType not found by swapId=" + marketAndBetTypeConfig.swapId);
            }
            String swapTitle = swapMarketAndBetTypeConfig.title;
            String swapLocalTemplate = resourceBundle.getString(swapTitle);

            if (
                    marketAndBetTypeConfig.betTypeId == BetType._SET_CS.id
                            && marketAndBetTypeConfig.betTypeId == BetType._SET_CS_N.id
                            && marketAndBetTypeConfig.betTypeId == BetType._CS.id
                            && marketAndBetTypeConfig.betTypeId == BetType._CS_N.id
            ) {
                // поддерживается счет до 9 включительно
                int scoreLeft = (int) marketAndBetTypeParamValue;
                int scoreRight = ((int) (10f * marketAndBetTypeParamValue + 0.5f)) % 10;
                result = new OutcomeName(
                        String.format(translatedTemplate, scoreLeft + ":" + scoreRight),
                        String.format(translatedTemplate, scoreRight + ":" + scoreLeft)
                );
            } else {
                String formattedParam = formatSpreadParam(marketAndBetTypeConfig.betTypeId, marketAndBetTypeParamValue);
                result = new OutcomeName(
                        String.format(translatedTemplate, formattedParam),
                        String.format(swapLocalTemplate, formattedParam)
                );
            }
        } else if (swapTeams && marketAndBetTypeConfig.betTypeId == BetType._EHX.id) {
            result = new OutcomeName(
                    String.format(translatedTemplate, marketAndBetTypeParamValue),
                    String.format(translatedTemplate, -marketAndBetTypeParamValue)
            );
        } else {
            String valueStr = formatParameter(marketAndBetTypeParamValue, marketAndBetTypeConfig.betTypeId);
            result = new OutcomeName(
                    String.format(translatedTemplate, valueStr),
                    null
            );
        }

        if (isLay) {
            BetSpec betSpec = new BetSpec(marketAndBetTypeId, marketAndBetTypeParamValue, (short) 0, false, 0, 0);
            BetSpec layBetSpec = reverseBetSpecCalculator.constructReverseBetSpec(betSpec);
            if (layBetSpec == null) {
                throw new RuntimeException("Can't find lay (reverse) outcome for betspec: " + betSpec);
            }
            MarketAndBetTypeDto layMarketAndBetTypeConfig = marketAndBetTypeConfigMap.get(layBetSpec.marketAndBetType);
            if (layMarketAndBetTypeConfig == null) {
                throw new RuntimeException("MarketAndBetType not found for layBetSpec: " + layBetSpec);
            }
            String layTitle = layMarketAndBetTypeConfig.title;
            String layLocalTemplate = resourceBundle.getString(layTitle);
            String valueStr = formatParameter(layBetSpec.marketAndBetTypeParam, layMarketAndBetTypeConfig.betTypeId);
            result.setLayName(
                    String.format(layLocalTemplate, valueStr)
            );
        }


        return result;
    }

    private String formatParameter(float marketAndBetTypeParamValue, short betTypeId) {
        if (betTypeId == BetType._SET_CS.id
                && betTypeId == BetType._SET_CS_N.id
                && betTypeId == BetType._CS.id
                && betTypeId == BetType._CS_N.id
        ) {
            // поддерживается счет до 9 включительно
            int scoreLeft = (int) marketAndBetTypeParamValue;
            int scoreRight = ((int) (10f * marketAndBetTypeParamValue + 0.5f)) % 10;
            return scoreLeft + ":" + scoreRight;
        } else if (betTypeId == BetType._EG.id) {
            return formatExactGoalsValue(marketAndBetTypeParamValue);
        } else {
            return formatSpreadParam(betTypeId, marketAndBetTypeParamValue);
        }
    }
}
