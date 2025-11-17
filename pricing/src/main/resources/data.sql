-- ============================================
-- PROMOTIONS SEED DATA FOR H2
-- ============================================

-- 1) 10% off on product 101
INSERT INTO promotion (
    id, active, name, description, type,
    discount_value, fixed_amount, buy_qty, get_qty,
    start_at, end_at, coupon_code
) VALUES (
    1, TRUE, '10% OFF Product 101', 'Seasonal discount 10% off',
    'PERCENTAGE',
    10.0, NULL, NULL, NULL,
    TIMESTAMP '2025-11-01 00:00:00',
    TIMESTAMP '2025-12-01 23:59:59',
    NULL
);

-- Applicable product IDs (element collection)
INSERT INTO promotion_applicable_product_ids (promotion_id, applicable_product_ids) VALUES (1, 1);



-- 2) $5 off for clearance products (product IDs 301, 302)
INSERT INTO promotion (
    id, active, name, description, type,
    discount_value, fixed_amount, buy_qty, get_qty,
    start_at, end_at, coupon_code
) VALUES (
    2, TRUE, 'Clearance $5 Off', 'Flat discount for clearance items',
    'FIXED',
    NULL, 5.0, NULL, NULL,
    TIMESTAMP '2025-01-01 00:00:00',
    TIMESTAMP '2030-12-31 23:59:59',
    NULL
);

INSERT INTO promotion_applicable_product_ids (promotion_id, applicable_product_ids) VALUES (2, 3);
INSERT INTO promotion_applicable_product_ids (promotion_id, applicable_product_ids) VALUES (2, 3);



-- 3) Buy 2 Get 1 Free on product 202
INSERT INTO promotion (
    id, active, name, description, type,
    discount_value, fixed_amount, buy_qty, get_qty,
    start_at, end_at, coupon_code
) VALUES (
    3, TRUE, 'Buy 2 Get 1 Free (Prod 202)', 'Auto apply B2G1 offer',
    'BUY_X_GET_Y',
    NULL, NULL, 2, 1,
    TIMESTAMP '2025-01-01 00:00:00',
    TIMESTAMP '2030-12-31 23:59:59',
    NULL
);

INSERT INTO promotion_applicable_product_ids (promotion_id, applicable_product_ids) VALUES (3, 2);



-- 4) Coupon SAVE20 → 20% off globally
INSERT INTO promotion (
    id, active, name, description, type,
    discount_value, fixed_amount, buy_qty, get_qty,
    start_at, end_at, coupon_code
) VALUES (
    4, TRUE, 'SAVE20 Coupon', '20% Off with coupon SAVE20',
    'PERCENTAGE',
    20.0, NULL, NULL, NULL,
    TIMESTAMP '2025-01-01 00:00:00',
    TIMESTAMP '2030-12-31 23:59:59',
    'SAVE20'
);

-- applies to ALL products (so no need to insert into applicable_product_ids)