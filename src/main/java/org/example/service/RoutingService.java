package org.example.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoutingService {

    private final CountryService countryService;

    public RoutingService(CountryService countryService) {
        this.countryService = countryService;
    }

    /**
     * Returns the shortest land route from origin to destination via BFS,
     * or null if no route exists or either country is unknown.
     */
    public Optional<List<String>> findRoute(String origin, String destination) {
        if (!countryService.countryExists(origin) || !countryService.countryExists(destination)) {
            return Optional.empty();
        }
        if (origin.equals(destination)) {
            return Optional.of(List.of(origin));
        }

        Map<String, Set<String>> graph = countryService.getBorderGraph();
        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        parent.put(origin, null);
        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (String neighbor : graph.getOrDefault(current, Set.of())) {
                if (visited.add(neighbor)) {
                    parent.put(neighbor, current);
                    if (neighbor.equals(destination)) {
                        return Optional.of(buildPath(parent, destination));
                    }
                    queue.add(neighbor);
                }
            }
        }
        return Optional.empty();
    }

    private List<String> buildPath(Map<String, String> parent, String destination) {
        List<String> path = new ArrayList<>();
        for (String node = destination; node != null; node = parent.get(node)) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }
}