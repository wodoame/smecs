# Smart E-Commerce System (SMECS)

Backend for an e-commerce platform built with Spring Boot, Spring Data JPA, PostgreSQL, GraphQL (dev), and Caffeine-based caching.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Database Configuration](#database-configuration)
4. [How to Run](#how-to-run)
5. [Caching](#caching)
6. [Project Structure](#project-structure)
7. [Features](#features)
8. [Troubleshooting](#troubleshooting)
9. [Testing](#testing)
10. [Documentation](#documentation)
11. [Security Event Logging](#security-event-logging)

---

## Prerequisites
- Java 21+ (pom sets `java.version` to 25)
- Maven 3.9+
- PostgreSQL running locally (`localhost:5432`)

---

## Project Setup
1. Install Java and Maven.
2. Configure PostgreSQL and create the `smecs` database (see below).
3. Ensure `application-dev.properties` matches your local credentials.

---

## Database Configuration
1. Create the database and user if needed:
   ```bash
   psql -U postgres -c "CREATE DATABASE smecs;"
   ```
2. Apply schema and indexes:
   ```bash
   psql -U postgres -d smecs -f src/main/resources/sql/schema.sql
   psql -U postgres -d smecs -f src/main/resources/sql/add_product_indexes.sql
   ```
3. Update credentials in `src/main/resources/application-dev.properties` if they differ from the defaults.

---

## How to Run
```bash
mvn spring-boot:run
```
- GraphiQL (dev): `http://localhost:8080/graphiql`

---

## Caching
Caching is enabled via `@EnableCaching` in `SmeCSApplication` and configured in `com.smecs.config.CacheConfig` using Caffeine.
- Cache names: `productsById`, `productSearch`, `categoriesById`, `categorySearch`
- Defaults: 5-minute TTL, max 1,000 entries
- Product and category services annotate read paths with `@Cacheable` and evict/refresh on writes.
- To add a cache: register a name in `CacheConfig`, annotate the service method, and evict affected caches on writes.
- See `docs/CACHING.md` for details.

---

## Project Structure
```
smecs/
├── docs/                     # Architecture and operational docs
│   ├── ARCHITECTURE.md
│   ├── CACHING.md
│   ├── PERFORMANCE_REPORT_TEMPLATE.md
│   ├── REPOSITORY_STRUCTURE.md
│   └── TRANSACTION_HANDLING.md
├── src/main/java/com/smecs/
│   ├── SmeCSApplication.java  # Spring Boot entrypoint (@EnableCaching)
│   ├── config/                # Cache config, other settings
│   ├── repository/            # Spring Data JPA repositories
│   ├── service/               # Business logic and transaction boundaries
│   ├── controller/            # Web/API controllers
│   └── util/                  # Utilities and helpers
├── src/main/resources/
│   ├── sql/                   # schema.sql, add_product_indexes.sql
│   └── application-dev.properties
└── frontend/                  # React/Vite frontend
```

---

## Features
- CRUD for products, categories, inventory, orders, and carts via Spring Data repositories
- Pagination and filtering support through specifications and pageable queries
- Caffeine caching for product/category lookups and searches
- GraphQL enabled for development

---

## Troubleshooting
- **Database connection issues:** Verify `spring.datasource.*` in `application-dev.properties` and that PostgreSQL is running.
- **Schema errors on startup:** Reapply `schema.sql` and `add_product_indexes.sql` to ensure the database matches the expected structure.
- **Stale data after updates:** Confirm cache eviction in the relevant service; see `docs/CACHING.md`.

---

## Testing
Run the test suite:
```bash
mvn test
```

---

## Documentation
- `docs/ARCHITECTURE.md` — system architecture and layering
- `docs/REPOSITORY_STRUCTURE.md` — repository conventions and usage
- `docs/TRANSACTION_HANDLING.md` — transaction boundaries and rollback guidance
- `docs/CACHING.md` — cache names, defaults, and extension steps
- `docs/PERFORMANCE_REPORT_TEMPLATE.md` — template for performance benchmarking
- `docs/SECURITY_CSRF_CORS.md` — CSRF vs CORS explanation and demo steps

---

## Security Event Logging

The backend captures authentication and token security events for auditing and brute-force detection.

- Login success/failure, OAuth2 success, token issuance, valid/invalid token usage are stored in `security_events`.
- Brute-force alerts are emitted after repeated failed logins from the same username/IP window.

### Reports (admin-only)

- `GET /api/security/reports/token-usage?start=2026-03-01T00:00:00Z&end=2026-03-05T23:59:59Z&limit=10`
- `GET /api/security/reports/brute-force?start=2026-03-01T00:00:00Z&end=2026-03-05T23:59:59Z&limit=10`

Both endpoints require `ROLE_ADMIN` and accept ISO-8601 `Instant` values for `start` and `end`.
