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

## Configuration

| Property | Default | Description |
|---|---|---|
| `countries.source.url` | GitHub `mledoze/countries` | URL of the `countries.json` data source |
| `route.cache.max-size` | `500` | Maximum number of routes kept in the LRU cache |

Properties can be overridden via `application.properties` or on the command line:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--route.cache.max-size=1000"
```
