package org.example.controller;

import org.example.model.RouteResponse;
import org.example.service.CountryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoutingControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private CountryService countryService;

    private static final Map<String, Set<String>> GRAPH = Map.of(
        "CZE", new HashSet<>(List.of("AUT", "DEU")),
        "AUT", new HashSet<>(List.of("CZE", "ITA")),
        "ITA", new HashSet<>(List.of("AUT")),
        "DEU", new HashSet<>(List.of("CZE")),
        "GBR", new HashSet<>(List.of("IRL")),
        "IRL", new HashSet<>(List.of("GBR"))
    );

    @BeforeEach
    void setUp() {
        when(countryService.getBorderGraph()).thenReturn(GRAPH);
        when(countryService.countryExists(anyString()))
            .thenAnswer(inv -> GRAPH.containsKey(inv.getArgument(0, String.class)));
    }

    @Test
    void returnsRouteFromCzeToIta() {
        ResponseEntity<RouteResponse> response =
            restTemplate.getForEntity("/routing/CZE/ITA", RouteResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().route()).containsExactly("CZE", "AUT", "ITA");
    }

    @Test
    void returnsBadRequestWhenNoLandRoute() {
        ResponseEntity<String> response =
            restTemplate.getForEntity("/routing/CZE/GBR", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void returnsBadRequestForUnknownCountry() {
        ResponseEntity<String> response =
            restTemplate.getForEntity("/routing/CZE/XXX", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void returnsSingleCountryRouteForSameOriginAndDestination() {
        ResponseEntity<RouteResponse> response =
            restTemplate.getForEntity("/routing/CZE/CZE", RouteResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().route()).containsExactly("CZE");
    }

    @Test
    void pathVariablesAreNormalisedToUpperCase() {
        ResponseEntity<RouteResponse> response =
            restTemplate.getForEntity("/routing/cze/ita", RouteResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().route()).containsExactly("CZE", "AUT", "ITA");
    }
}