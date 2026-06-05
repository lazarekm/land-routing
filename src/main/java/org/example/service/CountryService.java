package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.model.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class CountryService {

    private static final Logger log = LoggerFactory.getLogger(CountryService.class);

    @Value("${countries.source.url}")
    private String countriesUrl;

    private Map<String, Set<String>> borderGraph;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public CountryService(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    void loadCountries() {
        String json = restTemplate.getForObject(countriesUrl, String.class);
        List<Country> countries;
        try {
            countries = objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse country data", e);
        }
        Map<String, Set<String>> graph = new HashMap<>();
        for (Country c : countries) {
            graph.put(c.cca3(),
                c.borders() != null ? new HashSet<>(c.borders()) : new HashSet<>());
        }
        this.borderGraph = Collections.unmodifiableMap(graph);
        log.info("Loaded {} countries", borderGraph.size());
    }

    public Map<String, Set<String>> getBorderGraph() {
        return borderGraph;
    }

    public boolean countryExists(String cca3) {
        return borderGraph.containsKey(cca3);
    }
}