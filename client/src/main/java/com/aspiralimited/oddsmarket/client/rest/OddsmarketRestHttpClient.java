package com.aspiralimited.oddsmarket.client.rest;

import com.aspiralimited.oddsmarket.client.rest.dto.BetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.BookmakerDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketAndBetTypeDto;
import com.aspiralimited.oddsmarket.client.rest.dto.MarketDto;
import com.aspiralimited.oddsmarket.client.rest.dto.ResponseContainer;
import com.aspiralimited.oddsmarket.client.rest.dto.SportDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;

public class OddsmarketRestHttpClient {
    private final String baseUrlMst;
    private final String baseUrlLive;
    private final String baseUrlPrematch;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);


    public OddsmarketRestHttpClient(String baseUrlMst, String baseUrlLive, String baseUrlPrematch, long timeout) {
        this.baseUrlMst = baseUrlMst;
        this.baseUrlLive = baseUrlLive;
        this.baseUrlPrematch = baseUrlPrematch;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeout)).build();
    }

    public CompletableFuture<List<BetTypeDto>> getBetTypes() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/bet_types", new TypeReference<ResponseContainer<List<BetTypeDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    public CompletableFuture<List<BookmakerDto>> getBookmakers() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/bookmakers", new TypeReference<ResponseContainer<List<BookmakerDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    public CompletableFuture<List<MarketAndBetTypeDto>> getMarketAndBetTypes() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/market_and_bet_types", new TypeReference<List<MarketAndBetTypeDto>>() {
        });
    }

    public CompletableFuture<List<MarketDto>> getMarkets() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/markets", new TypeReference<ResponseContainer<List<MarketDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    public CompletableFuture<List<SportDto>> getSports() {
        return getGenericJsonEndpoint(baseUrlMst + "/v1/sports", new TypeReference<ResponseContainer<List<SportDto>>>() {
        })
                .thenApply(ResponseContainer::getResponse
                );
    }

    private <T> CompletableFuture<T> getGenericJsonEndpoint(String uri, TypeReference<T> typeReference) {
        return get(uri)
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        byte[] body = response.body();
                        try {
                            return objectMapper.readValue(body, typeReference);
                        } catch (IOException e) {
                            throw new RuntimeException("Error deserializing response body. Request URI: " + uri + "; response body: " + new String(body), e);
                        }
                    } else {
                        throw new RuntimeException("Invalid HTTP response code: " + response.statusCode()
                                + "; Response body: " + new String(response.body())
                                + "; Request URI: " + response.uri());
                    }
                });
    }


    public CompletableFuture<HttpResponse<byte[]>> get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("Accept", "application/json")
                .uri(URI.create(url)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
