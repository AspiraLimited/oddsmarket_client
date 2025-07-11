package com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.outcomenametranslator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
@Data
public class OutcomeName {
    // Effective outcome name (considering isLay and swapTeams flags)
    private String effectiveName;
    // Original outcome name
    private String originalName;
    // Lay outcome name
    private String layName;

    public OutcomeName(String effectiveName, String originalName) {
        this.effectiveName = effectiveName;
        this.originalName = originalName;
    }
}
