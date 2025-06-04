package com.aspiralimited.oddsmarket.api.v4.rest.dto.period;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@JacksonXmlRootElement(localName = "sports")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class SportPeriodsDto {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "sport")
    private List<SportEntry> sports;

    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    @ToString
    public static final class SportEntry {
        @JacksonXmlProperty(isAttribute = true)
        private short sportId;

        @JacksonXmlElementWrapper(localName = "periods")
        @JacksonXmlProperty(localName = "period")
        private List<PeriodDto> periods;
    }
}
