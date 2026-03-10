# Persistence Decisions: CascadeType and FetchType

## Selected rules

1. `Customer -> orders` uses `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY)`.
Reason: an order belongs to a specific customer in this model, and child rows should be managed together with the parent.

2. `Order -> customer` uses `@ManyToOne(fetch = LAZY)` without cascade.
Reason: customer is an aggregate root, and creating/updating an order must not auto-create or auto-delete customers.

3. `Order <-> Meal` uses `@ManyToMany(fetch = LAZY, cascade = {PERSIST, MERGE})`.
Reason: `PERSIST`/`MERGE` is enough for association lifecycle during order creation/editing, while `REMOVE` is intentionally avoided to prevent deleting shared meals.

4. `Meal -> Category` and `Meal -> Restaurant` use `@ManyToOne(fetch = LAZY)` without cascade.
Reason: category/restaurant are shared dictionaries, so meal operations must not remove or re-create dictionary entities.

5. `Category -> meals` and `Restaurant -> meals` use `@OneToMany(fetch = LAZY)`.
Reason: collections can be large and should not be fetched eagerly by default.

## N+1 handling

- The N+1 issue is demonstrated by loading orders with lazy associations via `findAll()` and mapping to DTO.
- The optimized path uses `@EntityGraph` in `OrderRepository#findAllWithDetails`.
- Result can be checked through endpoint `GET /api/v1/demo/n-plus-one`.
