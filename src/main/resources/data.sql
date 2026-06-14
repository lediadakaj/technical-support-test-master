-- Demo dataset. Recreated from scratch on every application startup.

INSERT INTO customers (id, name, email, phone, address, customer_since) VALUES
  (1, 'Sofia Mendes',   'sofia.mendes@example.com',   '+351 912 345 001', '12 Rua das Flores, 1200-192 Lisbon, Portugal', '2023-03-14'),
  (2, 'Liam O''Connor', 'liam.oconnor@example.com',   '+353 86 123 4502', NULL,                                           '2024-01-08'),
  (3, 'Amara Okafor',   'amara.okafor@example.com',   '+234 802 555 1203', '5 Marina Road, Victoria Island, Lagos, Nigeria', '2022-11-02'),
  (4, 'Jonas Weber',    'jonas.weber@example.com',    '+49 30 5550 1404', '88 Hauptstrasse, 10827 Berlin, Germany',       '2023-07-21'),
  (5, 'Yuki Tanaka',    'yuki.tanaka@example.com',    '+81 90 5555 1505', '3-2-1 Shibuya, Shibuya City, Tokyo, Japan',    '2024-05-30'),
  (6, 'Priya Sharma',   'priya.sharma@example.com',   '+91 98455 51606',  '42 MG Road, Bengaluru, Karnataka, India',      '2022-02-17'),
  (7, 'Mateo Rossi',    'mateo.rossi@example.com',    '+39 02 5550 1707', '7 Via Roma, 20121 Milan, Italy',               '2025-02-09'),
  (8, 'Hannah Berg',    'hannah.berg@example.com',    '+47 915 51 808',   '19 Storgata, 0155 Oslo, Norway',               '2023-09-12');

INSERT INTO orders (id, order_number, customer_id, status, discount_percent, placed_on, delivered_on) VALUES
  (1,  'ORD-1001', 1, 'DELIVERED', NULL, '2026-05-02', '2026-05-06'),
  (2,  'ORD-1002', 3, 'SHIPPED',   NULL, '2026-05-20', NULL),
  (3,  'ORD-1003', 4, 'DELIVERED', NULL, '2026-04-15', '2026-04-19'),
  (4,  'ORD-1004', 1, 'PENDING',   NULL, '2026-06-01', NULL),
  (5,  'ORD-1005', 5, 'DELIVERED', 10,   '2026-05-11', '2026-05-16'),
  (6,  'ORD-1006', 6, 'CANCELLED', NULL, '2026-03-28', NULL),
  (7,  'ORD-1007', 2, 'DELIVERED', NULL, '2026-05-25', '2026-05-29'),
  (8,  'ORD-1008', 7, 'PENDING',   NULL, '2026-06-08', NULL),
  (9,  'ORD-1009', 8, 'SHIPPED',   NULL, '2026-06-03', NULL),
  (10, 'ORD-1010', 3, 'DELIVERED', 15,   '2026-04-02', '2026-04-07'),
  (11, 'ORD-1011', 6, 'DELIVERED', NULL, '2026-05-29', '2026-06-04'),
  (12, 'ORD-1012', 1, 'PENDING',   NULL, '2026-06-10', NULL);

INSERT INTO order_items (id, order_id, product_name, unit_price, quantity) VALUES
  (1,  1,  'Wireless Mouse',              24.50,  1),
  (2,  1,  'Laptop Stand',                39.90,  1),
  (3,  2,  'Webcam 1080p',                59.00,  1),
  (4,  2,  'Ring Light',                  27.50,  2),
  (5,  3,  'Ergonomic Office Chair',     289.00,  1),
  (6,  4,  'USB-C Charging Cable',         9.99,  3),
  (7,  5,  'Mechanical Keyboard',         89.90,  1),
  (8,  5,  'USB-C Hub',                   20.00,  2),
  (9,  6,  'Desk Lamp',                   34.90,  1),
  (10, 7,  'Noise-Cancelling Headphones', 199.00, 1),
  (11, 8,  'Standing Desk',              449.00,  1),
  (12, 8,  'Cable Management Tray',       19.90,  2),
  (13, 9,  'External SSD 2TB',           159.00,  1),
  (14, 10, '4K Monitor 27"',             320.00,  2),
  (15, 11, 'Laptop Sleeve 14"',           29.90,  1),
  (16, 11, 'Wireless Charger',            35.00,  1),
  (17, 12, 'HDMI Cable 2m',               12.50,  2);

INSERT INTO order_notes (id, order_id, author, body, created_at) VALUES
  (1, 2, 'Dana (support)', 'Customer called asking for an ETA. Carrier tracking says Friday.', '2026-05-21 10:15:00'),
  (2, 6, 'Dana (support)', 'Cancelled at customer request before shipping. Refund processed.', '2026-03-29 09:02:00'),
  (3, 1, 'Ravi (support)', 'Delivery confirmed by customer. Closing the inquiry.',             '2026-05-07 16:40:00');

-- Notes are inserted at runtime; move the identity past the seeded ids.
ALTER TABLE order_notes ALTER COLUMN id RESTART WITH 100;
