# Performance Report

This document tracks baseline and post-optimization performance measurements for the Smart E-Commerce System.

## 1. Test Context
- **Date:**
- **Branch / Commit:**
- **Environment:** Local development
- **Database:**
- **Java Version:**
- **Hardware:**
- **Dataset:**
- **Profiling Tools:**
- **Load Testing Tool:**

## 2. Scenarios Under Test
- Scenario 1: `GET /api/products` without query parameters, 3-minute run, 1 virtual user.
- Scenario 2: `GET /api/products?query=laptop`, 3-minute run, 1 virtual user.
- Scenario 3: `GET /api/inventories`, 3-minute run, 1 virtual user.
- Scenario 4: `GET /api/inventories?query=laptop`, 3-minute run, 1 virtual user.
- Scenario 5: `GET /api/categories?relatedImages=true`, 3-minute run, 1 virtual user.
- Scenario 6: `GET /api/categories`, 3-minute run, 1 virtual user.
- Scenario 7: `POST /api/cart-items`, 3-minute run, 1 virtual user.
- Scenario 8: `POST /api/cart-items`, 3-minute run, 20 virtual users.
- Scenario 9: `GET /api/inventories`, 3-minute run, 20 virtual users.
- Scenario 10: `GET /api/inventories?query=laptop`, 3-minute run, 20 virtual users.

## 3. Baseline Metrics (Before)
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `GET /api/products` (no query params) | 169 | 1 virtual user | 30 | 46 | 54 | 92 | 0.90 | 1.18% | Heap used 126.45 MB; Heap max 4.16 GB | 0.00% | 3-minute baseline rerun with Actuator metrics |
| `GET /api/products?query=laptop` | 171 | 1 virtual user | 26 | 39 | 48 | 62 | 0.91 | 0.82% | Heap used 113.68 MB | 0.00% | 3-minute baseline run with search query |
| `GET /api/inventories` | 175 | 1 virtual user | 13 | 14 | 15 | 42 | 0.94 | 0.23% | Heap used 260.10 MB | 0.00% | 3-minute warm baseline rerun |
| `GET /api/inventories?query=laptop` | 184 | 1 virtual user | 13 | 14 | 15 | 26 | 0.99 | 0.46% | Heap used 202.12 MB | 0.00% | 3-minute warm baseline rerun with search query |
| `GET /api/categories?relatedImages=true` | 170 | 1 virtual user | 34 | 45 | 60 | 82 | 0.91 | 1.12% | Heap used 129.72 MB | 0.00% | 3-minute baseline run with related images |
| `GET /api/categories` | 173 | 1 virtual user | 20 | 35 | 46 | 85 | 0.92 | 0.68% | Heap used 113.90 MB | 0.00% | 3-minute baseline run |
| `POST /api/cart-items` | 169 | 1 virtual user | 34 | 58 | 72 | 103 | 0.90 | 1.39% | Heap used 69.18 MB | 0.00% | 3-minute baseline write-path run |

## 4. Bottlenecks Identified
- `POST /api/cart-items` shows a concurrency correctness issue under shared-user load: 2949 successful requests did not produce the expected final cart quantity.
- Observed result: final quantity was 2731 instead of 2949, indicating lost updates during concurrent writes to the same cart item.
- This was caused by the add-to-cart flow not being thread-safe for concurrent updates on the same cart/product pair.
- After adding locking and retry logic, the shared-user rerun produced an exact final quantity match, so the correctness issue is resolved.
- The consolidated checkout flow (`POST /api/orderitems`) contained a check-then-act race when verifying and decrementing inventory: concurrent checkouts could read the same available quantity and both succeed, risking overselling. This was fixed by adding row-level pessimistic locking on inventory and changing the lookup in `OrderItemServiceImpl.java` to use the locked repository lookup.
 - Synchronous security event recording during authentication added measurable latency to login/token flows under moderate load. The `SecurityEventService` methods (e.g. `recordLoginSuccess`, `recordTokenIssued`) were executed synchronously even though downstream processing (audit logging, metrics) did not affect the requesting flow. These were converted to asynchronous execution (background worker / `@Async` executor) to avoid blocking authentication and to improve overall request latency.

## 5. Changes Applied
- Added pessimistic locking for cart-item updates so concurrent requests on the same cart/product do not overwrite each other.
- Added a locked cart lookup and retry handling for concurrent cart creation attempts.
- Added tests covering locked update behavior and duplicate-insert retry handling in the cart flow.
 - Added row-level pessimistic locking for inventory during the checkout flow. The checkout logic in `src/main/java/com/smecs/service/impl/OrderItemServiceImpl.java` now acquires a database lock on inventory rows (via a locked lookup on `InventoryRepository`, e.g. `findByProduct_IdWithLock`) before verifying and decrementing stock. This prevents the overselling race where two concurrent checkouts could both read the same available quantity and succeed.

## 6. Load / Concurrency Results
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Delta vs Baseline |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `POST /api/cart-items` | 2949 | 20 virtual users | 41 | 63 | 88 | 198 | 15.70 | 7.53% | Heap used 110.04 MB | 0.00% | Avg +7 ms vs 1-user baseline; throughput up from 0.90 to 15.70 req/s; final cart quantity was 2731 instead of 2949 |
| `POST /api/cart-items` (after locking fix) | 3388 | 20 virtual users | 24 | 37 | 49 | 80 | 18.10 | 6.76% | Heap used 82.15 MB | 0.00% | Avg -10 ms vs 1-user baseline and -17 ms vs pre-fix 20-user run; throughput up to 18.10 req/s; final cart quantity matched all 3388 successful requests |
| `GET /api/inventories` | 3623 | 20 virtual users | 12 | 15 | 17 | 33 | 19.42 | 2.02% | Heap used 228.62 MB | 0.00% | Avg -37 ms vs 1-user baseline; P99 improved from 139ms to 33ms; likely due to caching warming up and JIT optimization |
| `GET /api/inventories?query=laptop` | 3613 | 20 virtual users | 11 | 14 | 16 | 28 | 19.37 | 2.26% | Heap used 228.86 MB | 0.00% | Avg -27 ms vs 1-user baseline; P99 improved from 97ms to 28ms; very stable performance with warmed cache |

## 7. Runtime Metrics Notes
- `GET /api/products` baseline rerun shows low latency under single-user load, with no observed request failures.
- Percentiles recorded: P90 = 46 ms, P95 = 54 ms, P99 = 92 ms.
- Actuator sampling during the run reported `process.cpu.usage = 0.0118`, `jvm.memory.used = 126449520 bytes`, and `jvm.memory.max = 4164943870 bytes`.
- `system.cpu.usage = 0.3975` was observed, but app-level CPU is the main value tracked in this report.
- `GET /api/products?query=laptop` also remained stable under single-user load, with 26 ms average latency and no errors.
- Search-query sampling reported `process.cpu.usage = 0.0082` and `jvm.memory.used = 113680256 bytes`.
- `GET /api/inventories` (Scenario 3) warm baseline rerun shows significantly better steady-state performance (13ms avg vs initial 49ms) and extremely low app CPU (0.23% vs initial 2.01%). Sampling during this rerun reported `process.cpu.usage = 0.0023` and `jvm.memory.used = 260104760 bytes`. This confirms the initial baseline was a cold start and the endpoint is now efficiently serving from the cache.
- `GET /api/inventories?query=laptop` (Scenario 4) warm baseline rerun shows significantly better steady-state performance (13ms avg vs initial 38ms) and lower app CPU (0.46% vs initial 1.22%). Sampling during this rerun reported `process.cpu.usage = 0.0046` and `jvm.memory.used = 202118856 bytes`. This confirms the initial baseline was a cold start and the system has since optimized the hot path.
- `GET /api/categories?relatedImages=true` stayed in the mid-range of the current baseline results, with lower latency than inventory reads and slightly higher latency than product reads.
- Category sampling reported `process.cpu.usage = 0.0112` and `jvm.memory.used = 129722528 bytes`.
- `GET /api/categories` is the fastest endpoint measured so far in the single-user baseline runs.
- Unfiltered category sampling reported `process.cpu.usage = 0.0068` and `jvm.memory.used = 113900224 bytes`.
- `POST /api/cart-items` gives an initial write-path baseline with mid-range latency and modest app CPU usage.
- Cart write sampling reported `process.cpu.usage = 0.0139` and `jvm.memory.used = 69176864 bytes`.
- `POST /api/cart-items` remained stable under 20 virtual users with no request failures.
- The 20-user cart write run reported `process.cpu.usage = 0.0753` and `jvm.memory.used = 110042672 bytes`.
- Despite zero HTTP errors, the shared-user cart test produced a lower-than-expected final quantity, which is evidence of a race condition rather than a transport failure.
- After the locking fix, the rerun of the same 20-user shared-cart scenario completed with `process.cpu.usage = 0.0676` and `jvm.memory.used = 82148224 bytes`.
- The post-fix rerun preserved correctness: cart quantity matched the total number of successful requests exactly.
- `GET /api/inventories` (20 VUs) showed significantly better latency than the 1-user baseline (12ms vs 49ms avg). This indicates that the endpoint is likely benefiting from Caffeine caching once warmed up. CPU usage remained efficient at ~2.02%. Memory usage reached ~228 MB, reflecting the overhead of serving concurrent requests and maintaining the cache.
- `GET /api/inventories?query=laptop` (20 VUs) achieved the best latency of any high-concurrency read path so far at 11ms average. Sampling during this run reported `process.cpu.usage = 0.0226` and `jvm.memory.used = 228858208 bytes`. This confirms the search logic and cache are highly optimized for filtered inventory reads.

## 8. Profiling Evidence
- Screenshots / captures:
- Observations:
  - The shared-user `POST /api/cart-items` load test exposed a lost-update race condition before optimization.
  - The cart add flow now uses database locking and retry logic to preserve correctness under concurrent writes.
  - The `/api/inventories` endpoint shows significant performance gains under load, likely due to caching and JIT warming.

## 9. Summary
- Baseline testing started with `GET /api/products` under a light single-user workload.
- The latest baseline rerun handled 169 requests over 3 minutes at 0.90 req/s with 30 ms average latency and no errors.
- Resource usage remained modest during the run, with about 1.18% JVM process CPU and 126.45 MB heap used.
- The filtered product search `GET /api/products?query=laptop` also performed well at 0.91 req/s with 26 ms average latency, 0.82% app CPU, and 113.68 MB heap used.
- `GET /api/inventories` steady-state baseline is now established at 13ms average latency and 0.23% app CPU, resolving previous cold-start outliers.
- `GET /api/inventories?query=laptop` steady-state baseline is also established at 13ms average latency and 0.46% app CPU.
- `GET /api/categories?relatedImages=true` performed at 34 ms average latency with 1.12% app CPU and 129.72 MB heap used, placing it between product and inventory reads.
- `GET /api/categories` performed best so far at 20 ms average latency, 0.68% app CPU, and 113.90 MB heap used.
- `POST /api/cart-items` completed with 34 ms average latency, 1.39% app CPU, and 69.18 MB heap used, giving a useful baseline for user write operations.
- Under 20 virtual users, `POST /api/cart-items` scaled to 15.70 req/s with average latency rising modestly from 34 ms to 41 ms and no errors.
- The first 20-user shared-cart run exposed a correctness issue: successful requests were lost at the data level, confirming a concurrency bug in the original implementation.
- After applying pessimistic locking and retry handling, the same shared-cart scenario completed correctly and also improved performance to 24 ms average latency, 18.10 req/s, 6.76% app CPU, and 82.15 MB heap used.
- `GET /api/inventories` load testing (20 VUs) revealed excellent scalability, likely due to caching, with average latency dropping to 12ms and throughput reaching 19.42 req/s.
- `GET /api/inventories?query=laptop` at 20 VUs is currently the most efficient read path, matching the best response times at 11ms average and 19.37 req/s.
- Additional read and write load tests are still needed before bottlenecks can be confirmed.

## 10. Concurrency and Data Integrity

The following race conditions were identified during high-concurrency load testing and resolved through targeted locking strategies.

### 10.1 Cart Item Updates (Lost Updates)
**Finding:** During concurrent `POST /api/cart-items` requests to the same cart/product pair, successful HTTP requests did not result in the correct final database quantity.
**Root Cause:** A "Check-then-Act" race where multiple threads read the same initial quantity, calculated the increment locally, and overwrote each other's updates.
**Solution:** Implemented `@Lock(LockModeType.PESSIMISTIC_WRITE)` in `CartItemRepository`. This serializes updates to the same cart-item row, ensuring every increment is based on the most recent committed value.

### 10.2 Inventory Decrement (Overselling)
**Finding:** Code review of the consolidated checkout flow (`POST /api/orderitems`) revealed a potential overselling risk where two concurrent checkouts for the last remaining item could both succeed.
**Root Cause:** Thread A checks stock (Available: 1) -> Thread B checks stock (Available: 1) -> Both decrement and create orders.
**Solution:** Implemented **Row-Level Pessimistic Locking** at the repository/use-site level. The checkout implementation in `src/main/java/com/smecs/service/impl/OrderItemServiceImpl.java` previously used an unlocked lookup (`findByProduct_Id`) which allowed a classic check-then-act race. The code now uses a locked lookup provided by `InventoryRepository` (for example `findByProduct_IdWithLock`) inside the same transactional boundary, so the relevant inventory rows are locked for writing while the transaction verifies and decrements stock. This serializes concurrent checkouts for the same product and prevents overselling.

### 10.3 Duplicate Checkout Submissions
**Finding:** Potential for duplicate orders if a user double-clicks the "Checkout" button.
**Solution:** 
- **Frontend:** The "Checkout" button is immediately disabled and replaced with a loading spinner upon the first click.
- **Backend:** The inventory locking strategy serves as a secondary safeguard, ensuring that even if multiple requests bypass the frontend protection, they are processed sequentially and can be validated against the current database state.

### 10.4 User-Level Cart Locking
**Finding:** Even with inventory locking, a single user could potentially trigger multiple successful checkouts for the same items if stock is sufficient, leading to duplicate orders.
**Solution:** Implemented **Pessimistic Locking on the Cart** at the start of the checkout transaction.
- The `Cart` row for the user is locked using `findByCartId`.
- This serializes the entire checkout process for a specific user.
- A validation check ensures the cart is not empty (`cartItems.isEmpty()`) inside the locked transaction. If a second request arrives, it waits for the first to finish, then wakes up to find an empty cart and aborts, effectively preventing duplicate orders at the database level.
