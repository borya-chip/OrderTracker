insert into customers (id, full_name, email, phone_number)
values (1, 'Demo Customer', 'demo.customer@example.com', '+375291111111');

insert into categories (id, name)
values (1, 'Burgers');

insert into restaurants (id, name, contact_email, city, address, phone, active)
values (1, 'Downtown Grill', 'contact@downtowngrill.example', 'Minsk', 'Lenina 10', '+375171234567', true);

insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (1, 'Classic Burger', 12.50, 15, 1, 1);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (2, 'Cheese Burger', 13.90, 17, 1, 1);

insert into orders (id, amount, date, description, customer_id)
values (1, 26.40, '2026-03-01T12:00:00', 'Lunch order', 1);

insert into order_meals (order_id, meal_id)
values (1, 1);
insert into order_meals (order_id, meal_id)
values (1, 2);
