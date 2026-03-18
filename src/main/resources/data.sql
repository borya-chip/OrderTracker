insert into customers (id, first_name, last_name, email, phone_number)
values (1, 'Alice', 'Johnson', 'alice.johnson@example.com', '+375291000001');
insert into customers (id, first_name, last_name, email, phone_number)
values (2, 'Bob', 'Miller', 'bob.miller@example.com', '+375291000002');
insert into customers (id, first_name, last_name, email, phone_number)
values (3, 'Clara', 'Davis', 'clara.davis@example.com', '+375291000003');
insert into customers (id, first_name, last_name, email, phone_number)
values (4, 'Daniel', 'Moore', 'daniel.moore@example.com', '+375291000004');
insert into customers (id, first_name, last_name, email, phone_number)
values (5, 'Eva', 'Taylor', 'eva.taylor@example.com', '+375291000005');
insert into customers (id, first_name, last_name, email, phone_number)
values (6, 'Frank', 'Wilson', 'frank.wilson@example.com', '+375291000006');
insert into customers (id, first_name, last_name, email, phone_number)
values (7, 'Grace', 'Anderson', 'grace.anderson@example.com', '+375291000007');
insert into customers (id, first_name, last_name, email, phone_number)
values (8, 'Henry', 'Thomas', 'henry.thomas@example.com', '+375291000008');
insert into customers (id, first_name, last_name, email, phone_number)
values (9, 'Ivy', 'Martin', 'ivy.martin@example.com', '+375291000009');
insert into customers (id, first_name, last_name, email, phone_number)
values (10, 'Jack', 'White', 'jack.white@example.com', '+375291000010');

insert into categories (id, name)
values (1, 'Burgers');
insert into categories (id, name)
values (2, 'Pizza');
insert into categories (id, name)
values (3, 'Sushi');
insert into categories (id, name)
values (4, 'Pasta');
insert into categories (id, name)
values (5, 'Salads');
insert into categories (id, name)
values (6, 'Desserts');
insert into categories (id, name)
values (7, 'Drinks');
insert into categories (id, name)
values (8, 'Seafood');
insert into categories (id, name)
values (9, 'Vegan');
insert into categories (id, name)
values (10, 'Breakfast');

insert into restaurants (id, name, contact_email, city, address, phone, active)
values (1, 'Downtown Grill', 'contact@downtowngrill.example', 'Minsk', 'Lenina 10', '+375171000001', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (2, 'River Pizza', 'contact@riverpizza.example', 'Minsk', 'Pobediteley 21', '+375171000002', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (3, 'Sakura Point', 'contact@sakurapoint.example', 'Minsk', 'Nemiga 7', '+375171000003', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (4, 'Pasta Corner', 'contact@pastacorner.example', 'Minsk', 'Kirova 14', '+375171000004', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (5, 'Green Bowl', 'contact@greenbowl.example', 'Minsk', 'Pritytskogo 55', '+375171000005', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (6, 'Sweet Lab', 'contact@sweetlab.example', 'Minsk', 'Kalvariyskaya 18', '+375171000006', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (7, 'Burger Hub', 'contact@burgerhub.example', 'Minsk', 'Surganova 31', '+375171000007', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (8, 'Ocean Catch', 'contact@oceancatch.example', 'Minsk', 'Masherova 40', '+375171000008', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (9, 'Veggie Table', 'contact@veggietable.example', 'Minsk', 'Yanki Kupaly 9', '+375171000009', true);
insert into restaurants (id, name, contact_email, city, address, phone, active)
values (10, 'Morning Bite', 'contact@morningbite.example', 'Minsk', 'Nezavisimosti 88', '+375171000010', true);

insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (1, 'Classic Burger', 12.50, 15, 1, 1);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (2, 'Pepperoni Pizza', 18.90, 20, 2, 2);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (3, 'Salmon Roll Set', 24.50, 18, 3, 3);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (4, 'Chicken Carbonara', 16.20, 17, 4, 4);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (5, 'Caesar Salad', 11.40, 12, 5, 5);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (6, 'Chocolate Cheesecake', 9.80, 8, 6, 6);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (7, 'Double Smash Burger', 13.20, 14, 1, 7);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (8, 'Grilled Salmon', 22.70, 19, 8, 8);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (9, 'Vegan Bowl', 14.60, 11, 9, 9);
insert into meals (id, name, price, cooking_time, category_id, restaurant_id)
values (10, 'Avocado Toast', 10.90, 10, 10, 10);

insert into orders (id, amount, date, description, customer_id)
values (1, 12.50, '2026-03-10T12:00:00', 'Lunch burger order', 1);
insert into orders (id, amount, date, description, customer_id)
values (2, 18.90, '2026-03-10T12:30:00', 'Office pizza order', 2);
insert into orders (id, amount, date, description, customer_id)
values (3, 24.50, '2026-03-11T13:15:00', 'Evening sushi order', 3);
insert into orders (id, amount, date, description, customer_id)
values (4, 16.20, '2026-03-12T18:40:00', 'Pasta takeaway', 4);
insert into orders (id, amount, date, description, customer_id)
values (5, 11.40, '2026-03-13T14:05:00', 'Healthy lunch salad', 5);
insert into orders (id, amount, date, description, customer_id)
values (6, 9.80, '2026-03-14T11:30:00', 'Dessert break order', 6);
insert into orders (id, amount, date, description, customer_id)
values (7, 13.20, '2026-03-15T16:20:00', 'Burger dinner order', 7);
insert into orders (id, amount, date, description, customer_id)
values (8, 22.70, '2026-03-16T19:00:00', 'Seafood evening order', 8);
insert into orders (id, amount, date, description, customer_id)
values (9, 14.60, '2026-03-17T20:10:00', 'Vegan bowl order', 9);
insert into orders (id, amount, date, description, customer_id)
values (10, 10.90, '2026-03-18T09:15:00', 'Breakfast order', 10);

insert into order_meals (order_id, meal_id)
values (1, 1);
insert into order_meals (order_id, meal_id)
values (2, 2);
insert into order_meals (order_id, meal_id)
values (3, 3);
insert into order_meals (order_id, meal_id)
values (4, 4);
insert into order_meals (order_id, meal_id)
values (5, 5);
insert into order_meals (order_id, meal_id)
values (6, 6);
insert into order_meals (order_id, meal_id)
values (7, 7);
insert into order_meals (order_id, meal_id)
values (8, 8);
insert into order_meals (order_id, meal_id)
values (9, 9);
insert into order_meals (order_id, meal_id)
values (10, 10);
