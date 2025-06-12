package com.aspiralimited.oddsmarket.api.v4.rest.dto.period;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public final class PeriodDto {
    @JacksonXmlProperty(isAttribute = true)
    private short periodIdentifier;
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    @JacksonXmlProperty(isAttribute = true)
    private Short nextPeriodIdentifier;
    @JacksonXmlProperty(isAttribute = true)
    private Short upperLevelPeriodIdentifier;
}
