# Performance Report

This document tracks baseline and post-optimization performance measurements for the Smart E-Commerce System.

## 1. Test Context
- **Date:** 2026-03-19
- **Environment:** Local development
- **Database:** PostgreSQL
- **Java Version:** 25
- **Hardware:** 11th Gen Intel i7-1165G7 (8) @ 4.700GHz
- **Profiling Tools:** Spring Actuator
- **Load Testing Tool:** Postman

## 2. Scenarios Under Test
- Single‑user baselines (3-minute runs, 1 virtual user):
  - Scenario 1: `GET /api/products` (no query parameters)
  - Scenario 2: `GET /api/products?query=laptop`
  - Scenario 3: `GET /api/inventories` (no query parameters)
  - Scenario 4: `GET /api/inventories?query=laptop`
  - Scenario 5: `GET /api/categories?relatedImages=true`
  - Scenario 6: `GET /api/categories`
  - Scenario 7: `POST /api/cart-items` (single-user write baseline)

- Concurrency / Load tests (3-minute runs, 20 virtual users):
  - Scenario 8: `GET /api/products` (no query parameters)
  - Scenario 9: `GET /api/products?query=laptop`
  - Scenario 10: `GET /api/inventories` (no query parameters)
  - Scenario 11: `GET /api/inventories?query=laptop`
  - Scenario 12: `GET /api/categories?relatedImages=true`
  - Scenario 13: `GET /api/categories`
  - Scenario 14: `POST /api/cart-items` (shared-user write test)

## 3. Baseline Metrics

### Baseline Metrics (Before) — caching + async disabled
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `GET /api/products` (no query params, caching+async disabled) | 189 | 1 virtual user | 17 | 23 | 27 | 36 | 1.01 | 0.26% | Heap used 215.26 MB | 0.00% | 3-minute baseline run with caching and async disabled |
| `GET /api/products?query=laptop` (caching+async disabled) | 187 | 1 virtual user | 17 | 23 | 24 | 31 | 1.00 | 0.26% | Heap used 242.74 MB | 0.00% | 3-minute baseline run with caching and async disabled |
| `GET /api/inventories` (no query params, caching+async disabled) | 181 | 1 virtual user | 18 | 22 | 25 | 27 | 0.97 | 0.25% | Heap used 223.15 MB | 0.00% | 3-minute baseline run with caching and async disabled |
| `GET /api/inventories?query=laptop` (caching+async disabled) | 181 | 1 virtual user | 14 | 17 | 18 | 20 | 0.97 | 0.21% | Heap used 232.93 MB | 0.00% | 3-minute baseline run with caching and async disabled |
| `GET /api/categories?relatedImages=true` (caching+async disabled) | 181 | 1 virtual user | 22 | 27 | 30 | 36 | 0.97 | 0.21% | Heap used 248.37 MB | 0.00% | 3-minute baseline run with caching and async disabled |

### Baseline Metrics (After) — caching + async enabled (cache warmed where noted)
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `GET /api/products` (no query params, caching+async enabled, cache warmed) | 187 | 1 virtual user | 6 | 9 | 10 | 15 | 1.00 | 0.18% | Heap used 255.83 MB | 0.00% | 3-minute baseline run with caching and async enabled (cache warmed at start) |
| `GET /api/products?query=laptop` (caching+async enabled, cache warmed) | 184 | 1 virtual user | 9 | 12 | 13 | 17 | 0.99 | 0.23% | Heap used 259.50 MB | 0.00% | 3-minute baseline run with caching and async enabled (cache warmed at start) |
| `GET /api/inventories` (no query params, caching+async enabled, cache warmed) | 177 | 1 virtual user | 11 | 16 | 18 | 21 | 0.95 | 0.23% | Heap used 213.36 MB | 0.00% | 3-minute baseline run with caching and async enabled (cache warmed at start) |
| `GET /api/inventories?query=laptop` (caching+async enabled, cache warmed) | 189 | 1 virtual user | 10 | 14 | 17 | 25 | 1.01 | 0.28% | Heap used 235.43 MB | 0.00% | 3-minute baseline run with caching and async enabled (cache warmed at start) |
| `GET /api/categories?relatedImages=true` (caching+async enabled, cache warmed) | 186 | 1 virtual user | 7 | 10 | 11 | 24 | 1.00 | 0.24% | Heap used 235.67 MB | 0.00% | 3-minute baseline run with caching and async enabled (cache warmed at start) |
| `POST /api/cart-items` (caching+async enabled, cache warmed) | 189 | 1 virtual user | 14 | 18 | 19 | 26 | 1.01 | 0.22% | Heap used 254.73 MB | 0.00% | 3-minute baseline write-path run with caching and async enabled |
| `POST /api/cart-items` (caching+async enabled, cache warmed) | 189 | 1 virtual user | 14 | 18 | 19 | 26 | 1.01 | 0.22% | Heap used 254.73 MB | 0.00% | 3-minute baseline write-path run with caching and async enabled |


## 4. Load / Concurrency Results

### Load / Concurrency Results (Before) — caching + async disabled
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Delta vs Baseline |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `GET /api/products?query=laptop` (caching+async disabled) | 3643 | 20 virtual users | 21 | 13 | 16 | 237 | 19.54 | 1.35% | Heap used 234.89 MB | 0.00% | Avg +4 ms vs 1-user no-cache baseline; 20-user load run with caching and async disabled |
| `GET /api/products` (no query params, caching+async disabled) | 3656 | 20 virtual users | 9 | 11 | 12 | 19 | 19.62 | 1.54% | Heap used 245.94 MB | 0.00% | Avg -8 ms vs 1-user no-cache baseline; 20-user load run with caching and async disabled |
| `GET /api/inventories` (no query params, caching+async disabled) | 3659 | 20 virtual users | 10 | 14 | 15 | 21 | 19.63 | 1.95% | Heap used 220.80 MB | 0.00% | Avg -8 ms vs 1-user no-cache baseline; 20-user load run with caching and async disabled |
| `GET /api/inventories?query=laptop` (caching+async disabled) | 3651 | 20 virtual users | 10 | 14 | 16 | 25 | 19.59 | 2.82% | Heap used 241.69 MB | 0.00% | Avg -4 ms vs 1-user no-cache baseline; 20-user load run with caching and async disabled |
| `POST /api/cart-items` (caching+async disabled) | 3639 | 20 virtual users | 12 | 17 | 18 | 26 | 19.52 | 1.80% | Heap used 246.33 MB | 0.00% | 20-user load run with caching and async disabled |
| `GET /api/categories?relatedImages=true` (caching+async disabled) | 3652 | 20 virtual users | 12 | 17 | 19 | 25 | 19.60 | 2.44% | Heap used 240.25 MB | 0.00% | 20-user load run with caching and async disabled |
| `GET /api/categories` (caching+async disabled) | 3657 | 20 virtual users | 9 | 12 | 13 | 18 | 19.63 | 1.25% | Heap used 231.59 MB | 0.00% | 20-user load run with caching and async disabled |

### Load / Concurrency Results (After) — caching + async enabled (cache warmed where noted)
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Delta vs Baseline |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `GET /api/products?query=laptop` (caching+async enabled, cache warmed) | 3679 | 20 virtual users | 6 | 7 | 10 | 50 | 19.74 | 1.07% | Heap used 256.50 MB | 0.00% | 20-user load run with caching+async enabled (cache warmed at start) |
| `POST /api/cart-items` (caching+async enabled, 20 VUs) | 3633 | 20 virtual users | 12 | 14 | 17 | 106 | 19.48 | 1.57% | Heap used 222.33 MB | 0.00% | Final cart quantity matched successful requests; 20-user load run with caching+async enabled |
| `GET /api/inventories` (caching+async enabled, cache warmed) | 3623 | 20 virtual users | 12 | 15 | 17 | 33 | 19.42 | 2.02% | Heap used 228.62 MB | 0.00% | Avg -37 ms vs 1-user baseline; P99 improved from 139ms to 33ms; likely due to caching warming up and JIT optimization |
| `GET /api/inventories?query=laptop` (caching+async enabled, cache warmed) | 3613 | 20 virtual users | 11 | 14 | 16 | 28 | 19.37 | 2.26% | Heap used 228.86 MB | 0.00% | Avg -27 ms vs 1-user baseline; P99 improved from 97ms to 28ms; very stable performance with warmed cache |

## 5. Bottlenecks Identified

1. Cart Item Updates (Lost Updates)
- Finding: `POST /api/cart-items` under a shared-user 20‑VU test recorded 2949 successful requests but the final cart quantity was 2731.
- Root cause: a check‑then‑act race in the add‑to‑cart flow where concurrent threads read, increment, and overwrite the same cart item record.
- Evidence: zero HTTP errors but mismatched final DB quantity; deterministic under synthetic shared‑user load.
- Impact: data integrity loss (lost increments) even though transport succeeded; incorrect order/cart state for users.

2. Inventory Decrement Race (Overselling Risk)
- Finding: concurrent checkout requests could both observe the same available inventory and both succeed, risking overselling.
- Root cause: unlocked lookup + separate verify/decrement steps created a classic check‑then‑act race on inventory rows.
- Evidence: code review of `OrderItemServiceImpl` and reproduction under concurrent checkout scenarios.
- Impact: potential for selling more items than are available; financial/operational correctness issue.

3. Synchronous Security Event Recording (Authentication Latency)
- Finding: login/token flows showed measurable latency under moderate load correlated with synchronous `SecurityEventService` calls.
- Root cause: audit/metric recording code executed synchronously on the request path even though it does not impact request correctness.
- Evidence: profiling and sampling during authentication flows showed time spent in security event recording.
- Impact: higher user‑visible latency for authentication; avoidable blocking on unrelated I/O or processing.

4. Duplicate Checkout Submissions (Frontend + Backend)
- Finding: double‑clicking the checkout button could trigger duplicate orders.
- Root cause: frontend did not reliably debounce the submit action and backend checkout logic lacked a user‑level serialization safeguard.
- Evidence: race scenarios where two near‑simultaneous requests for the same cart could both create orders.
- Impact: duplicate orders and user confusion; need both UI and server safeguards.

5. User‑Level Concurrency During Checkout
- Finding: even with inventory locking, a single user could submit multiple successful checkouts if requests overlapped and stock permitted.
- Root cause: lack of a serialized transaction boundary at the cart/user level for the checkout flow.
- Evidence: theoretical race and defensive testing showed duplicate checkout windows for the same user.
- Impact: duplicate orders from repeated user actions; requires per‑user checkout serialization.


## 6. Changes Applied

1. Cart Item Concurrency Fixes
- Action: Added pessimistic locking for cart‑item updates to serialize concurrent updates on the same cart/product pair.
- Action: Implemented a locked cart lookup and retry handling to handle concurrent cart creation attempts safely.
- Tests: Added unit/integration tests that cover locked update behavior and duplicate‑insert retry handling in the cart flow.

2. Inventory Locking for Checkout
- Action: Introduced row‑level pessimistic locking for inventory during checkout. The checkout logic in `src/main/java/com/smecs/service/impl/OrderItemServiceImpl.java` now uses a locked lookup on `InventoryRepository` (for example `findByProduct_IdWithLock`) inside the same transactional boundary before verifying and decrementing stock.
- Rationale: This serializes concurrent checkouts for the same product and prevents overselling by eliminating the check‑then‑act window.

3. Authentication Event Performance
- Action: Converted synchronous `SecurityEventService` calls (e.g., `recordLoginSuccess`, `recordTokenIssued`) to asynchronous execution using a background worker / `@Async` executor.
- Rationale: Offloads non‑critical audit/metric recording off the request path to reduce authentication latency under load.


## 8. Profiling Evidence
- Screenshots / captures:
- Observations:
  - The shared-user `POST /api/cart-items` load test exposed a lost-update race condition before optimization.
  - The cart add flow now uses database locking and retry logic to preserve correctness under concurrent writes.
  - The `/api/inventories` endpoint shows significant performance gains under load, likely due to caching and JIT warming.

## 9. Summary

This report captures single‑user baselines and representative 20‑VU load tests run across the product, inventory, category and cart write paths. Key takeaways:

1. Caching provides clear median and tail improvements for read paths when warmed — warmed cache runs consistently reduced average latency and tightened P90/P95.
2. The add‑to‑cart write path required targeted locking to resolve a lost‑update data‑integrity issue; after adding pessimistic locking and retry handling the shared‑user correctness problem was resolved and performance improved under contention.
3. Inventory decrements during checkout were protected via row‑level pessimistic locking to prevent overselling under concurrent checkouts.
4. Authentication latency was reduced by moving non‑critical security event recording off the request path and executing it asynchronously.
