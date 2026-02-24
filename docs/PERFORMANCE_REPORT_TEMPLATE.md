# Performance Report Template

Use this template to record pre/post optimization results, caching impact, and regression checks.

## 1. Test Context
- **Date:**
- **Branch / Commit:**
- **Database:** (engine, version, host)
- **Java Version:**
- **Hardware:** (CPU, memory)
- **Dataset:** (row counts, seed method)

## 2. Scenarios
List the scenarios you measured (e.g., product search, category listing, order checkout).
- Scenario 1:
- Scenario 2:
- Scenario 3:

## 3. Baseline Metrics (Before)
| Scenario | P50 (ms) | P95 (ms) | Throughput (req/s) | Notes |
|----------|----------|----------|--------------------|-------|
|          |          |          |                    |       |

## 4. Changes Applied
- Code changes:
- Query/index tweaks:
- Caching adjustments:
- Configuration flags:

## 5. Results (After)
| Scenario | P50 (ms) | P95 (ms) | Throughput (req/s) | Delta vs Baseline |
|----------|----------|----------|--------------------|-------------------|
|          |          |          |                    |                   |

## 6. Caching Impact
- Cache provider: (Caffeine)
- Cache names: `productsById`, `productSearch`, `categoriesById`, `categorySearch`
- TTL / size: 5 minutes, 1,000 entries (defaults)
- Hit rate observations:
- Eviction considerations:

## 7. Query Notes
Document any JPQL/native queries or specifications you tuned, plus index usage.
- Query:
- Plan highlights:
- Index changes:

## 8. Regressions / Risks
- Any latency spikes or errors observed?
- Memory/CPU impacts?
- Follow-up actions:

## 9. Summary
A short narrative of the improvements and next steps.

