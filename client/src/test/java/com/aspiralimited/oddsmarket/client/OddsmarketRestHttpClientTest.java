package com.aspiralimited.oddsmarket.client;

import com.aspiralimited.oddsmarket.api.v4.rest.dto.BookmakerDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.CountryDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.InternalEventDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.LeagueDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.MarketAndBetTypeDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.MarketDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.PlayerDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.SportDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.period.PeriodDto;
import com.aspiralimited.oddsmarket.api.v4.rest.dto.period.SportPeriodsDto;
import com.aspiralimited.oddsmarket.client.v4.rest.OddsmarketRestHttpClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class OddsmarketRestHttpClientTest {
    static final int WIREMOCK_PORT = 33456;
    static final String WIREMOCK_BASE_URL = "http://localhost:" + WIREMOCK_PORT;
    private static WireMockServer wireMockServer;

    final OddsmarketRestHttpClient oddsmarketRestHttpClient = new OddsmarketRestHttpClient(WIREMOCK_BASE_URL, WIREMOCK_BASE_URL, 500);

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor(WIREMOCK_PORT);
    }

    @AfterAll
    static void cleanup() {
        wireMockServer.stop();
    }

    @SneakyThrows
    @Test
    void shouldParseBookmakersResponse() {
        makeWiremockStub("/v4/bookmakers", "/rest-response-samples/bookmakers.json");

        List<BookmakerDto> actual = oddsmarketRestHttpClient.getBookmakers().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    @Test
    void shouldParseMarketAndBetTypesResponse() {
        makeWiremockStub("/v4/market_and_bet_types", "/rest-response-samples/market_and_bet_types.json");

        List<MarketAndBetTypeDto> actual = oddsmarketRestHttpClient.getMarketAndBetTypes().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    @Test
    void shouldParseMarketsResponse() {
        makeWiremockStub("/v4/markets", "/rest-response-samples/markets.json");

        List<MarketDto> actual = oddsmarketRestHttpClient.getMarkets().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    @Test
    void shouldParseSportsResponse() {
        makeWiremockStub("/v4/sports", "/rest-response-samples/sports.json");

        List<SportDto> actual = oddsmarketRestHttpClient.getSports().get();
        Assertions.assertFalse(actual.isEmpty());
    }

    @SneakyThrows
    @Test
    void shouldParsePeriodNameResponse() {
        makeWiremockStub("/v4/periodName?identifier=0&sportId=6", "/rest-response-samples/periodName.txt");

        String actual = oddsmarketRestHttpClient.getPeriodName((short) 0, (short) 6).get();
        Assertions.assertEquals("regular time", actual);
    }

    @SneakyThrows
    @Test
    void shouldParseInternalEventsResponse() {
        makeWiremockStub("/v4/internal_events/374092363,374518899", "/rest-response-samples/internal_events.json");

        List<InternalEventDto> actual = oddsmarketRestHttpClient.getInternalEvents(List.of(374092363L, 374518899L)).get();
        Assertions.assertEquals(2, actual.size());
    }

    @SneakyThrows
    @Test
    void shouldParseLeaguesResponse() {
        makeWiremockStub("/v4/leagues/5944,8719", "/rest-response-samples/leagues.json");

        List<LeagueDto> actual = oddsmarketRestHttpClient.getLeagues(List.of(5944L, 8719L)).get();
        Assertions.assertEquals(2, actual.size());
    }

    @SneakyThrows
    @Test
    void shouldParseCountriesResponse() {
        List<CountryDto> expected = List.of(
                new CountryDto(2, "Italy", "it", "ITA", "italy"),
                new CountryDto(3, "Brazil", "br", "BRA", "brazil")
        );
        makeWiremockStub("/v4/countries?sportId=7", "/rest-response-samples/countries.json");

        List<CountryDto> actual = oddsmarketRestHttpClient.getCountries((short) 7).get();
        Assertions.assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void shouldParsePlayersResponse() {
        makeWiremockStub("/v4/players/3654837", "/rest-response-samples/players.json");

        List<PlayerDto> actual = oddsmarketRestHttpClient.getPlayers(List.of(3654837L)).get();
        Assertions.assertEquals(1, actual.size());
    }

    @SneakyThrows
    @Test
    void shouldParseSportPeriodsResponse() {
        SportPeriodsDto expected = new SportPeriodsDto(
                List.of(
                        new SportPeriodsDto.SportEntry(
                                (short) 4,
                                List.of(
                                        new PeriodDto((short) -200, "To Win Outright", null, null),
                                        new PeriodDto((short) -100, "To Qualify", null, null),
                                        new PeriodDto((short) 0, "Regular time", null, null),
                                        new PeriodDto((short) 1, "1st Half", (short) 2, null),
                                        new PeriodDto((short) 2, "2nd Half", null, null)
                                )
                        ),
                        new SportPeriodsDto.SportEntry(
                                (short) 41,
                                List.of(
                                        new PeriodDto((short) -1, "Full time with overtimes", null, null),
                                        new PeriodDto((short) 0, "Regular time", null, null),
                                        new PeriodDto((short) 1, "1st Quarter", (short) 2, (short) 10),
                                        new PeriodDto((short) 2, "2nd Quarter", (short) 3, (short) 10),
                                        new PeriodDto((short) 3, "3rd Quarter", (short) 4, (short) 20),
                                        new PeriodDto((short) 4, "4th Quarter", null, (short) 20),
                                        new PeriodDto((short) 10, "1st Half", (short) 20, null),
                                        new PeriodDto((short) 20, "2nd Half", null, null)
                                )
                        )
                )
        );

        makeWiremockStub("/v4/periods?sportId=4&sportId=41", "/rest-response-samples/sport_periods.json");

        SportPeriodsDto periods = oddsmarketRestHttpClient.getPeriods(List.of((short) 4, (short) 41)).get();

        Assertions.assertEquals(expected, periods);
    }

    @SneakyThrows
    private void makeWiremockStub(String url, String filename) {
        byte[] body = getClass().getResourceAsStream(filename).readAllBytes();
        WireMock.stubFor(
                WireMock.get(url)
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(body)
                        )
        );
    }


}
