package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private CountryService countryService;

    @InjectMocks
    private RoutingService routingService;

    // Small graph:  CZE -- AUT -- ITA
    //               |
    //              DEU
    //   GBR -- IRL  (isolated cluster)
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
        lenient().when(countryService.getBorderGraph()).thenReturn(GRAPH);
        when(countryService.countryExists(anyString()))
            .thenAnswer(inv -> GRAPH.containsKey(inv.getArgument(0, String.class)));
    }

    @Test
    void findsDirectBorderRoute() {
        assertThat(routingService.findRoute("CZE", "AUT"))
            .hasValueSatisfying(r -> assertThat(r).containsExactly("CZE", "AUT"));
    }

    @Test
    void findsTwoHopRoute() {
        assertThat(routingService.findRoute("CZE", "ITA"))
            .hasValueSatisfying(r -> assertThat(r).containsExactly("CZE", "AUT", "ITA"));
    }

    @Test
    void returnsSingleElementRouteForSameCountry() {
        assertThat(routingService.findRoute("CZE", "CZE"))
            .hasValueSatisfying(r -> assertThat(r).containsExactly("CZE"));
    }

    @Test
    void returnsEmptyWhenNoLandRouteExists() {
        assertThat(routingService.findRoute("CZE", "GBR")).isEmpty();
    }

    @Test
    void returnsEmptyForUnknownOrigin() {
        assertThat(routingService.findRoute("XXX", "ITA")).isEmpty();
    }

    @Test
    void returnsEmptyForUnknownDestination() {
        assertThat(routingService.findRoute("CZE", "XXX")).isEmpty();
    }

    @Test
    void routeIsSymmetric() {
        Optional<List<String>> forward = routingService.findRoute("CZE", "ITA");
        Optional<List<String>> reverse = routingService.findRoute("ITA", "CZE");
        assertThat(forward).isPresent();
        assertThat(reverse).isPresent();
        assertThat(forward.get()).hasSameSizeAs(reverse.get());
    }
}