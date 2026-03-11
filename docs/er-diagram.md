# ER Diagram

```mermaid
erDiagram
    CUSTOMER ||--o{ ORDER : places
    ORDER }o--o{ MEAL : contains
    CATEGORY ||--o{ MEAL : groups
    RESTAURANT ||--o{ MEAL : offers

    CUSTOMER {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar email
        varchar phone_number
    }

    ORDER {
        bigint id PK
        double amount
        timestamp date
        varchar description
        bigint customer_id FK
    }

    MEAL {
        bigint id PK
        varchar name
        numeric price
        int cooking_time
        bigint category_id FK
        bigint restaurant_id FK
    }

    CATEGORY {
        bigint id PK
        varchar name
    }

    RESTAURANT {
        bigint id PK
        varchar name
        varchar contact_email
        varchar city
        varchar address
        varchar phone
        boolean active
    }

    ORDER_MEALS {
        bigint order_id PK, FK
        bigint meal_id PK, FK
    }
```

## PK/FK summary

- `orders.customer_id -> customers.id`
- `meals.category_id -> categories.id`
- `meals.restaurant_id -> restaurants.id`
- `order_meals.order_id -> orders.id`
- `order_meals.meal_id -> meals.id`
