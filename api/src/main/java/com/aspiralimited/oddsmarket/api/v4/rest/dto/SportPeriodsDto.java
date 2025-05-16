package com.aspiralimited.oddsmarket.api.v4.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class SportPeriodsDto {
    public List<SportEntry> sports;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static final class SportEntry {
        public short sportId;
        public List<PeriodEntry> periods;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static final class PeriodEntry {
        public short periodIdentifier;
        public String name;
        public Short nextPeriodIdentifier;
        public Short upperLevelPeriodIdentifier;
    }
}
