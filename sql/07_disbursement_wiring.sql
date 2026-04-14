USE rvs_fishworld;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'receiving_headers' AND COLUMN_NAME = 'amount_paid') = 0,
              'ALTER TABLE receiving_headers ADD COLUMN amount_paid DECIMAL(14,4) NOT NULL DEFAULT 0 AFTER total_amount',
              'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'receiving_headers' AND COLUMN_NAME = 'cash_on_hand') = 0,
              'ALTER TABLE receiving_headers ADD COLUMN cash_on_hand DECIMAL(14,4) NOT NULL DEFAULT 0 AFTER amount_paid',
              'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE receiving_headers
SET cash_on_hand = CASE WHEN cash_on_hand = 0 THEN total_amount ELSE cash_on_hand END
WHERE cash_on_hand = 0;

CREATE TABLE IF NOT EXISTS disbursements (
    disbursement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ref_no VARCHAR(40) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    description TEXT,
    fish_purchase_amount DECIMAL(14,4) NOT NULL DEFAULT 0,
    fish_commission_amount DECIMAL(14,4) NOT NULL DEFAULT 0,
    trucking_amount DECIMAL(14,4) NOT NULL DEFAULT 0,
    cash_on_hand_amount DECIMAL(14,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(14,4) NOT NULL DEFAULT 0,
    prepared_by VARCHAR(100),
    checked_by VARCHAR(100),
    approved_by VARCHAR(100),
    received_by VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_disbursement_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

CREATE TABLE IF NOT EXISTS disbursement_details (
    disbursement_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    disbursement_id BIGINT NOT NULL,
    receiving_id BIGINT NOT NULL,
    rr_no VARCHAR(40) NOT NULL,
    rr_amount DECIMAL(14,4) NOT NULL DEFAULT 0,
    rr_adj DECIMAL(14,4) NOT NULL DEFAULT 0,
    rr_total DECIMAL(14,4) NOT NULL DEFAULT 0,
    remarks VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_disb_detail_header FOREIGN KEY (disbursement_id) REFERENCES disbursements(disbursement_id) ON DELETE CASCADE,
    CONSTRAINT fk_disb_detail_receiving FOREIGN KEY (receiving_id) REFERENCES receiving_headers(receiving_id)
);
