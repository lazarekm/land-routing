# land-routing

REST API that calculates the shortest land route between two countries using BFS.

## Requirements

- Java 21
- Maven 3.8+
- Internet access (country data is fetched from GitHub on startup)

## Build

```bash
mvn clean package
```

## Run

```bash
mvn spring-boot:run
```

The server starts on port 8080.

## API

### GET /routing/{origin}/{destination}

Returns the shortest sequence of country codes (cca3) forming a land route.

**Path variables** are case-insensitive (`CZE` = `cze`).

**Success (200)**

```
GET /routing/CZE/ITA
{"route":["CZE","AUT","ITA"]}
```

**No land route or unknown country (400)**

```
GET /routing/CZE/AUS
HTTP 400 Bad Request
```

## Test

```bash
mvn test
```

Unit tests cover `RoutingService` in isolation (Mockito). Integration tests cover the full HTTP stack with a mocked `CountryService` so no network access is required during tests.

## Design

| Class | Responsibility |
|---|---|
| `CountryService` | Fetches `countries.json` at startup, exposes the border graph |
| `RoutingService` | BFS over the border graph; returns `null` when no route exists |
| `RoutingController` | Translates `null` → 400, route → 200 with JSON body |