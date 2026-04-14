USE rvs_fishworld;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'receiving_headers' AND COLUMN_NAME = 'is_cancelled') = 0,
              'ALTER TABLE receiving_headers ADD COLUMN is_cancelled BOOLEAN NOT NULL DEFAULT FALSE AFTER total_amount',
              'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE trans_shippers
MODIFY legacy_parent_customer_code VARCHAR(100) NULL;

ALTER TABLE receiving_headers
MODIFY total_amount DECIMAL(14,4) NOT NULL DEFAULT 0;

ALTER TABLE receiving_details
MODIFY total_cost DECIMAL(14,4) NOT NULL DEFAULT 0;

ALTER TABLE inventory_ledger
MODIFY total_cost DECIMAL(14,4) NOT NULL DEFAULT 0;
