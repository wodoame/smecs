# Transaction Handling

This guide documents how transactions are applied in the service layer and how to design new workflows that remain consistent and safe.

## Principles
- Keep `@Transactional` at the **service layer** to avoid leaking persistence rules into controllers.
- Default propagation is `REQUIRED`; a service method joins an existing transaction or opens one when none exists.
- Rollback happens automatically on unchecked exceptions (`RuntimeException`, `IllegalStateException`, etc.). For checked exceptions, set `rollbackFor` explicitly when needed.
- Write methods (create/update/delete) own cache eviction so caches and database stay in sync.

## Current Usage
- Inventory flows (`InventoryServiceImpl`) wrap create, update, and delete operations in transactions to keep stock quantities aligned with product state and to invalidate caches after successful commits.
- Order and cart DAOs/services apply `@Transactional` on write paths so multi-step workflows (e.g., saving order + items) succeed or fail atomically.

## Patterns for New Workflows
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orders;
    private final PaymentClient paymentClient;

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = orders.save(map(request));
        paymentClient.charge(request.payment()); // let RuntimeException trigger rollback
        return order;
    }
}
```
- Keep external calls (payments, messaging) inside the transaction only when they must abort the database work on failure. Otherwise, handle them after commit via events or retries.
- Validate inputs before starting long-running work to minimize lock time.
- For read-only queries, leave methods non-transactional unless repeatable reads are required.

## Failure & Rollback Checklist
- Throw a RuntimeException (or use `rollbackFor`) when business rules fail (e.g., insufficient stock).
- Ensure cache eviction happens **after** the database write succeeds; avoid evicting inside partial updates that may roll back.
- When combining multiple repositories, prefer a single transaction boundary per service method to avoid nested transactions unless required by isolation.

## Isolation Considerations
- Default isolation is database-specific (usually `READ_COMMITTED`). Adjust via `@Transactional(isolation = Isolation.SERIALIZABLE)` only when concurrency demands it.
- Use optimistic locking fields on entities when concurrent updates are common; pessimistic locks should be reserved for contention hot spots.

