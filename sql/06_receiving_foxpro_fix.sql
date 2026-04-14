USE rvs_fishworld;

-- explanation: this fixes known FoxPro cost data needed by Receiving line entry
UPDATE products
SET unit_of_measure = 'PCS',
    previous_cost = 150.0000,
    average_cost = 150.0000
WHERE product_code = '30001S';

-- explanation: this keeps BANDED SHARK show-size active even if the row was loaded with zero cost
UPDATE products
SET is_active = TRUE
WHERE product_code = '30001S';

-- explanation: use this check after the update so you can confirm the cost really exists in MySQL
SELECT product_code, description, unit_of_measure, previous_cost, average_cost
FROM products
WHERE product_code = '30001S';
