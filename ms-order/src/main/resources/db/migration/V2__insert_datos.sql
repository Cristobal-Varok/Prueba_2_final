-- Insertar datos de prueba
INSERT INTO orders (username, status, payment_id, payment_status, total_amount, created_at, updated_at)
VALUES ('juan.perez', 'PENDING', NULL, 'PENDING', 36.0, NOW(), NOW());

INSERT INTO order_items (order_id, product_id, quantity, price)
VALUES (1, 3, 2, 18.0);

INSERT INTO orders (username, status, payment_id, payment_status, total_amount, created_at, updated_at)
VALUES ('maria.gomez', 'PAID', 'PAY-98765', 'PAID', 125.50, NOW(), NOW());

INSERT INTO order_items (order_id, product_id, quantity, price)
VALUES (2, 4, 1, 125.50);