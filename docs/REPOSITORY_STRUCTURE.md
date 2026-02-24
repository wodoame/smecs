# Repository Structure

This guide explains how the repository layer is organized and how to build new Spring Data repositories that stay consistent with the existing codebase.

## Location & Conventions
- Package: `src/main/java/com/smecs/repository`
- Interfaces extend `JpaRepository<Aggregate, IdType>`; add `JpaSpecificationExecutor<T>` when filtering and sorting are needed.
- Use clear names for derived queries (e.g., `findByUserId`, `findTop5ProductImageUrlsByCategoryId`).
- Keep custom SQL/JPA queries small and focused; prefer specifications for composable filters.

## Key Repositories
- `ProductRepository` → CRUD + specifications for products.
- `CategoryRepository` → Derived/native queries for product counts and thumbnail URLs.
- `OrderRepository` → Paging helpers `findByUserId` and `findByStatus` for order history/operations dashboards.
- `OrderItemRepository` → Fetch order line items by order id.
- `InventoryRepository` → CRUD + specifications and `findByProductId` to align stock with catalog items.
- `UserRepository` → Lookups by username/email with `exists` guards for sign-up flows.

## Specifications and Custom Implementations
- `CategorySpecification` / `ProductSpecification` encapsulate reusable predicates for filtered lists.
- `InventoryRepositoryCustom` + `InventoryRepositoryCustomImpl` house search and pagination logic that does not fit derived queries.
- Prefer adding new reusable predicates to a `*Specification` class instead of embedding ad-hoc filters in services.

## Patterns for New Repositories
```java
@Repository
public interface ExampleRepository extends JpaRepository<Example, Long>, JpaSpecificationExecutor<Example> {
    Optional<Example> findByExternalId(String externalId);

    @Query("select e from Example e where e.status = :status")
    Page<Example> findActive(@Param("status") Status status, Pageable pageable);
}
```

- Expose paging through `Pageable` to keep controllers/services consistent.
- Use `@Query` only when derived queries or specifications cannot express the logic clearly.
- Keep write operations inside services so transactions and cache eviction remain centralized.

## Where to Plug In
- Add new repository interfaces under `com.smecs.repository`.
- Place shared filters in a `*Specification` class under the same package.
- For complex searches, add a `*RepositoryCustom` interface and `*RepositoryCustomImpl` class to separate custom JPQL/SQL from the core interface.

