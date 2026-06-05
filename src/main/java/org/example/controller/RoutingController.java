package org.example.controller;

import org.example.model.RouteResponse;
import org.example.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/routing")
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<RouteResponse> getRoute(
            @PathVariable String origin,
            @PathVariable String destination) {
        Optional<List<String>> route = routingService.findRoute(origin.toUpperCase(), destination.toUpperCase());
        return route.map(r -> ResponseEntity.ok(new RouteResponse(r)))
                    .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}