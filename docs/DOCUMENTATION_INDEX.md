# Smart E-Commerce System (SMECS) - Documentation Index

## Start Here
- **Quick start (backend)**: see `README.md` → prerequisites, database setup, and how to run (`mvn spring-boot:run`).
- **What this app is**: Spring Boot backend with Spring Data JPA, Caffeine caching, PostgreSQL, GraphQL enabled for dev.

## Core References
- `README.md` — setup, running locally, caching configuration, troubleshooting.
- `docs/ARCHITECTURE.md` — layered design (controller → service → repository) and component overview.
- `docs/REPOSITORY_STRUCTURE.md` — repository conventions, key interfaces, and guidance for adding queries/specifications.
- `docs/TRANSACTION_HANDLING.md` — how `@Transactional` is applied, rollback patterns, and when to tune isolation.
- `docs/CACHING.md` — cache names, TTL/size defaults, eviction patterns, and how to add new caches.
- `docs/PERFORMANCE_REPORT_TEMPLATE.md` — template for capturing before/after metrics and cache impact.
- `docs/SECURITY_CSRF_CORS.md` — CSRF vs CORS explanation and Postman/browser demo steps.
- `DESIGN_SYSTEM.md` — UI design references for the frontend.

## By Role / Task
- **Set up and run locally**: `README.md` (prerequisites, database config, run command).
- **Extend data access**: `docs/REPOSITORY_STRUCTURE.md`, `docs/ARCHITECTURE.md`.
- **Add/modify transactions**: `docs/TRANSACTION_HANDLING.md`.
- **Tune caching**: `docs/CACHING.md`.
- **Report performance work**: `docs/PERFORMANCE_REPORT_TEMPLATE.md`.
- **Understand CORS/CSRF behavior**: `docs/SECURITY_CSRF_CORS.md`.

## Quick Links
- Backend entrypoint: `src/main/java/com/smecs/SmeCSApplication.java`
- Data access package: `src/main/java/com/smecs/repository`
- Services (transaction boundaries): `src/main/java/com/smecs/service`
- Cache config: `src/main/java/com/smecs/config/CacheConfig.java`

**Status:** Spring Data + caching integrated. Keep docs in sync with code when adding repositories, transactions, or cache names.
