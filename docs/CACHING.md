# Caching Guide

This guide explains how caching is configured and how to extend it safely.

## Defaults
- Provider: Caffeine (see `com.smecs.config.CacheConfig`)
- Cache names: `productsById`, `productSearch`, `categoriesById`, `categorySearch`
- TTL: 5 minutes
- Max size: 1,000 entries per cache
- Enabled via `@EnableCaching` on `SmeCSApplication`

## Current Usage
- Product flows (`ProductServiceImpl`) cache product lookups and search results; write operations evict or refresh entries.
- Category flows (`CategoryServiceImpl`) cache category details (with/without images) and search results, with key helpers for composed cache keys.
- Inventory search uses a dedicated in-memory helper (`InventoryCacheService`) to memoize paged responses.

## Adding a New Cache
1. Add a cache name constant in `CacheConfig` and register it in the `CaffeineCacheManager` bean.
2. Annotate service methods with `@Cacheable`, `@CachePut`, or `@CacheEvict` using the new cache name.
3. Ensure write paths evict/refresh any affected read caches to avoid stale data.
4. Keep key generation stable and deterministic (prefer simple ids and primitive values).

## Operational Tips
- Keep cached payloads small; avoid caching huge result sets.
- Evict search/list caches when underlying data changes.
- Monitor memory if adding large caches; adjust `maximumSize` or TTL as needed.

## Switching Providers
If you need Redis or another provider later:
- Replace `CaffeineCacheManager` with the desired cache manager bean.
- Keep cache names stable so annotations continue to work.
- Revisit TTL/size settings appropriate to the new provider.

