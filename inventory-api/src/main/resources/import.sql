-- Categories
INSERT INTO category (id, name) VALUES (1, 'Electronics');
INSERT INTO category (id, name) VALUES (2, 'Clothing');
INSERT INTO category (id, name) VALUES (3, 'Home Appliances');

-- Products
INSERT INTO product (id, name, price, quantity, category_id) VALUES (1, 'Laptop', 1200.00, 15, 1);
INSERT INTO product (id, name, price, quantity, category_id) VALUES (2, 'Smartphone', 800.00, 25, 1);
INSERT INTO product (id, name, price, quantity, category_id) VALUES (3, 'Headphones', 150.00, 40, 1);
INSERT INTO product (id, name, price, quantity, category_id) VALUES (4, 'T-Shirt', 20.00, 100, 2);
INSERT INTO product (id, name, price, quantity, category_id) VALUES (5, 'Washing Machine', 550.00, 10, 3);
INSERT INTO product (id, name, price, quantity, category_id) VALUES (6, 'Microwave Oven', 180.00, 8, 3);
