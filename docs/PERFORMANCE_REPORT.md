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

## 3. Baseline Metrics (Before)
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `GET /api/products` (no query params) | 169 | 1 virtual user | 30 | 46 | 54 | 92 | 0.90 | 1.18% | Heap used 126.45 MB; Heap max 4.16 GB | 0.00% | 3-minute baseline rerun with Actuator metrics |
| `GET /api/products?query=laptop` | 171 | 1 virtual user | 26 | 39 | 48 | 62 | 0.91 | 0.82% | Heap used 113.68 MB | 0.00% | 3-minute baseline run with search query |
| `GET /api/inventories` | 168 | 1 virtual user | 49 | 82 | 95 | 139 | 0.90 | 2.01% | Heap used 128.91 MB | 0.00% | 3-minute baseline run |
| `GET /api/inventories?query=laptop` | 168 | 1 virtual user | 38 | 62 | 73 | 97 | 0.90 | 1.22% | Heap used 144.46 MB | 0.00% | 3-minute baseline run with search query |
| `GET /api/categories?relatedImages=true` | 170 | 1 virtual user | 34 | 45 | 60 | 82 | 0.91 | 1.12% | Heap used 129.72 MB | 0.00% | 3-minute baseline run with related images |
| `GET /api/categories` | 173 | 1 virtual user | 20 | 35 | 46 | 85 | 0.92 | 0.68% | Heap used 113.90 MB | 0.00% | 3-minute baseline run |
| `POST /api/cart-items` | 169 | 1 virtual user | 34 | 58 | 72 | 103 | 0.90 | 1.39% | Heap used 69.18 MB | 0.00% | 3-minute baseline write-path run |

## 4. Bottlenecks Identified
- `POST /api/cart-items` shows a concurrency correctness issue under shared-user load: 2949 successful requests did not produce the expected final cart quantity.
- Observed result: final quantity was 2731 instead of 2949, indicating lost updates during concurrent writes to the same cart item.
- This was caused by the add-to-cart flow not being thread-safe for concurrent updates on the same cart/product pair.
- After adding locking and retry logic, the shared-user rerun produced an exact final quantity match, so the correctness issue is resolved.

## 5. Changes Applied
- Added pessimistic locking for cart-item updates so concurrent requests on the same cart/product do not overwrite each other.
- Added a locked cart lookup and retry handling for concurrent cart creation attempts.
- Added tests covering locked update behavior and duplicate-insert retry handling in the cart flow.

## 6. Load / Concurrency Results
| Scenario | Requests | Concurrency | Avg (ms) | P90 (ms) | P95 (ms) | P99 (ms) | Throughput (req/s) | CPU | Memory | Errors | Delta vs Baseline |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `POST /api/cart-items` | 2949 | 20 virtual users | 41 | 63 | 88 | 198 | 15.70 | 7.53% | Heap used 110.04 MB | 0.00% | Avg +7 ms vs 1-user baseline; throughput up from 0.90 to 15.70 req/s; final cart quantity was 2731 instead of 2949 |
| `POST /api/cart-items` (after locking fix) | 3388 | 20 virtual users | 24 | 37 | 49 | 80 | 18.10 | 6.76% | Heap used 82.15 MB | 0.00% | Avg -10 ms vs 1-user baseline and -17 ms vs pre-fix 20-user run; throughput up to 18.10 req/s; final cart quantity matched all 3388 successful requests |

## 7. Runtime Metrics Notes
- `GET /api/products` baseline rerun shows low latency under single-user load, with no observed request failures.
- Percentiles recorded: P90 = 46 ms, P95 = 54 ms, P99 = 92 ms.
- Actuator sampling during the run reported `process.cpu.usage = 0.0118`, `jvm.memory.used = 126449520 bytes`, and `jvm.memory.max = 4164943870 bytes`.
- `system.cpu.usage = 0.3975` was observed, but app-level CPU is the main value tracked in this report.
- `GET /api/products?query=laptop` also remained stable under single-user load, with 26 ms average latency and no errors.
- Search-query sampling reported `process.cpu.usage = 0.0082` and `jvm.memory.used = 113680256 bytes`.
- `GET /api/inventories` is slower than the two product-list scenarios in the same single-user baseline runs.
- Inventory sampling reported `process.cpu.usage = 0.0201` and `jvm.memory.used = 128907696 bytes`.
- `GET /api/inventories?query=laptop` performed better than the unfiltered inventory request on latency and CPU, but used more heap during sampling.
- Filtered inventory sampling reported `process.cpu.usage = 0.0122` and `jvm.memory.used = 144455592 bytes`.
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

## 8. Profiling Evidence
- Screenshots / captures:
- Observations:
  - The shared-user `POST /api/cart-items` load test exposed a lost-update race condition before optimization.
  - The cart add flow now uses database locking and retry logic to preserve correctness under concurrent writes.

## 9. Summary
- Baseline testing started with `GET /api/products` under a light single-user workload.
- The latest baseline rerun handled 169 requests over 3 minutes at 0.90 req/s with 30 ms average latency and no errors.
- Resource usage remained modest during the run, with about 1.18% JVM process CPU and 126.45 MB heap used.
- The filtered product search `GET /api/products?query=laptop` also performed well at 0.91 req/s with 26 ms average latency, 0.82% app CPU, and 113.68 MB heap used.
- `GET /api/inventories` showed higher latency and CPU than the product endpoints: 49 ms average latency, 2.01% app CPU, and 128.91 MB heap used.
- `GET /api/inventories?query=laptop` improved over unfiltered inventory reads to 38 ms average latency and 1.22% app CPU, though sampled heap usage increased to 144.46 MB.
- `GET /api/categories?relatedImages=true` performed at 34 ms average latency with 1.12% app CPU and 129.72 MB heap used, placing it between product and inventory reads.
- `GET /api/categories` performed best so far at 20 ms average latency, 0.68% app CPU, and 113.90 MB heap used.
- `POST /api/cart-items` completed with 34 ms average latency, 1.39% app CPU, and 69.18 MB heap used, giving a useful baseline for user write operations.
- Under 20 virtual users, `POST /api/cart-items` scaled to 15.70 req/s with average latency rising modestly from 34 ms to 41 ms and no errors.
- The first 20-user shared-cart run exposed a correctness issue: successful requests were lost at the data level, confirming a concurrency bug in the original implementation.
- After applying pessimistic locking and retry handling, the same shared-cart scenario completed correctly and also improved performance to 24 ms average latency, 18.10 req/s, 6.76% app CPU, and 82.15 MB heap used.
- Additional read and write load tests are still needed before bottlenecks can be confirmed.
