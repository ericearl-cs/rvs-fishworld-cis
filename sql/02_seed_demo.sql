USE rvs_fishworld;

INSERT INTO branches (branch_code, branch_name)
VALUES ('MAIN', 'MAIN WAREHOUSE')
ON DUPLICATE KEY UPDATE branch_name = VALUES(branch_name);

INSERT INTO currencies (currency_code, currency_name)
VALUES ('USD', 'AMERICAN'),
       ('PHP', 'PHILIPPINE PESO')
ON DUPLICATE KEY UPDATE currency_name = VALUES(currency_name);

INSERT INTO categories (category_code, category_name, sort_code)
VALUES ('01', 'ANGEL', '01'),
       ('05', 'BLENNY', '04'),
       ('11', 'GOBIES', '02')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), sort_code = VALUES(sort_code);

INSERT INTO brands (brand_code, brand_name)
VALUES ('CONSU', 'CONSUMABLE PRODUCTS')
ON DUPLICATE KEY UPDATE brand_name = VALUES(brand_name);

INSERT INTO suppliers (supplier_code, supplier_name, supplier_address, contact_person, is_active)
VALUES ('GA1', 'GA SUPPLIER 1', 'BATANGAS', 'JUAN', TRUE),
       ('MI1', 'MI SUPPLIER 1', 'SURIGAO', 'PEDRO', TRUE),
       ('RM', 'RM SUPPLIER', 'ZAMBALES', 'MARIA', TRUE),
       ('RVS', 'RVS FIELD', 'PALAWAN', 'RICKIE', TRUE)
ON DUPLICATE KEY UPDATE supplier_name = VALUES(supplier_name);

INSERT INTO products (
    product_code, description, scientific_name, category_id, brand_id, unit_of_measure,
    total_quantity, previous_cost, average_cost, price_b, is_active
)
SELECT '01001', 'BANDED ANGEL (REEF SAFE)', 'PARACENTROPYGE MULTIFASCIATU',
       c.category_id, NULL, 'PCS', 0, 0, 0, 20.00, TRUE
FROM categories c
WHERE c.category_code = '01'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO products (
    product_code, description, scientific_name, category_id, brand_id, unit_of_measure,
    total_quantity, previous_cost, average_cost, price_b, is_active
)
SELECT '01005', 'BLUE BELLUS ANGEL FEMALE (REEF SAFE)', 'GENICANTHUS BELLUS',
       c.category_id, NULL, 'PCS', 0, 0, 0, 55.00, TRUE
FROM categories c
WHERE c.category_code = '01'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO products (
    product_code, description, scientific_name, category_id, brand_id, unit_of_measure,
    total_quantity, previous_cost, average_cost, price_b, is_active
)
SELECT '01008', 'BLUE FACE ANGEL ADULT (M/L)', 'EUXIPHIPOPS XANTHOMETAPON',
       c.category_id, NULL, 'PCS', 0, 0, 0, 45.00, TRUE
FROM categories c
WHERE c.category_code = '01'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO products (
    product_code, description, scientific_name, category_id, brand_id, unit_of_measure,
    total_quantity, previous_cost, average_cost, price_b, is_active
)
SELECT '05008', 'RED LIZARD BLENNY', 'PARAPERCIS SCHAUSILANDII',
       c.category_id, NULL, 'PCS', 0, 0, 0, 0.94, TRUE
FROM categories c
WHERE c.category_code = '05'
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO products (
    product_code, description, scientific_name, category_id, brand_id, unit_of_measure,
    total_quantity, previous_cost, average_cost, price_b, is_active
)
SELECT '11008', 'BLUE DOTTED PEACOCK GOBY', 'CRYPTOCENTRUS PAVONINOIDES',
       c.category_id, NULL, 'PCS', 0, 0, 0, 0.50, TRUE
FROM categories c
WHERE c.category_code = '11'
ON DUPLICATE KEY UPDATE description = VALUES(description);
