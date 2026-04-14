CREATE DATABASE IF NOT EXISTS rvs_fishworld;
USE rvs_fishworld;

CREATE TABLE IF NOT EXISTS branches (
    branch_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(20) NOT NULL UNIQUE,
    branch_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS currencies (
    currency_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    currency_code VARCHAR(10) NOT NULL UNIQUE,
    currency_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(30) NOT NULL UNIQUE,
    supplier_name VARCHAR(200) NOT NULL,
    supplier_address VARCHAR(255),
    contact_person VARCHAR(100),
    position_title VARCHAR(100),
    telephone_no VARCHAR(50),
    fax_no VARCHAR(50),
    zip_code VARCHAR(20),
    exempt_stop_forever BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);



CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(30) NOT NULL UNIQUE,
    customer_name VARCHAR(200) NOT NULL,
    customer_address VARCHAR(255),
    contact_person VARCHAR(100),
    position_title VARCHAR(100),
    zip_code VARCHAR(20),
    telephone_no VARCHAR(50),
    fax_no VARCHAR(50),
    discount_percent DECIMAL(12,2) NOT NULL DEFAULT 0,
    remarks TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trans_shippers (
    trans_shipper_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trans_shipper_code VARCHAR(30) NOT NULL UNIQUE,
    trans_shipper_name VARCHAR(255) NOT NULL,
    parent_customer_id BIGINT NULL,
    legacy_parent_customer_code VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trans_shipper_customer FOREIGN KEY (parent_customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(20) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    sort_code VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS brands (
    brand_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_code VARCHAR(20) NOT NULL UNIQUE,
    brand_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    scientific_name VARCHAR(255),
    category_id BIGINT NOT NULL,
    brand_id BIGINT NULL,
    unit_of_measure VARCHAR(20),
    is_invertebrate BOOLEAN NOT NULL DEFAULT FALSE,
    extended_description TEXT,
    reorder_point DECIMAL(12,2) NOT NULL DEFAULT 0,
    maximum_point DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_quantity DECIMAL(12,2) NOT NULL DEFAULT 0,
    previous_cost DECIMAL(12,4) NOT NULL DEFAULT 0,
    average_cost DECIMAL(12,4) NOT NULL DEFAULT 0,
    price_a DECIMAL(12,2) NOT NULL DEFAULT 0,
    price_b DECIMAL(12,2) NOT NULL DEFAULT 0,
    price_c DECIMAL(12,2) NOT NULL DEFAULT 0,
    price_d DECIMAL(12,2) NOT NULL DEFAULT 0,
    price_e DECIMAL(12,2) NOT NULL DEFAULT 0,
    price_f DECIMAL(12,2) NOT NULL DEFAULT 0,
    price_g DECIMAL(12,2) NOT NULL DEFAULT 0,
    special_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    local_sales_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    deliveries_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(brand_id)
);

CREATE TABLE IF NOT EXISTS receiving_headers (
    receiving_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rr_no VARCHAR(40) NOT NULL UNIQUE,
    date_received DATE NOT NULL,
    direct_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    supplier_id BIGINT NOT NULL,
    ltp_no VARCHAR(50),
    branch_id BIGINT NOT NULL,
    currency_id BIGINT NOT NULL,
    encoded_by VARCHAR(100),
    checked_by VARCHAR(100),
    approved_by VARCHAR(100),
    total_amount DECIMAL(14,4) NOT NULL DEFAULT 0,
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_receiving_header_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    CONSTRAINT fk_receiving_header_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id),
    CONSTRAINT fk_receiving_header_currency FOREIGN KEY (currency_id) REFERENCES currencies(currency_id)
);

CREATE TABLE IF NOT EXISTS receiving_details (
    receiving_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receiving_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    group_code VARCHAR(20),
    product_id BIGINT NOT NULL,
    qty_delivered DECIMAL(12,2) NOT NULL DEFAULT 0,
    qty_doa DECIMAL(12,2) NOT NULL DEFAULT 0,
    qty_rejected DECIMAL(12,2) NOT NULL DEFAULT 0,
    reject_reason VARCHAR(150),
    tank VARCHAR(50),
    qty_bought DECIMAL(12,2) NOT NULL DEFAULT 0,
    unit_cost DECIMAL(12,4) NOT NULL DEFAULT 0,
    total_cost DECIMAL(14,4) NOT NULL DEFAULT 0,
    stop_flag BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_receiving_detail_header FOREIGN KEY (receiving_id) REFERENCES receiving_headers(receiving_id) ON DELETE CASCADE,
    CONSTRAINT fk_receiving_detail_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE IF NOT EXISTS inventory_ledger (
    inventory_ledger_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_type VARCHAR(50) NOT NULL,
    reference_id BIGINT NOT NULL,
    reference_no VARCHAR(50) NOT NULL,
    line_no INT NOT NULL,
    transaction_date DATE NOT NULL,
    product_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    qty_in DECIMAL(12,2) NOT NULL DEFAULT 0,
    qty_out DECIMAL(12,2) NOT NULL DEFAULT 0,
    unit_cost DECIMAL(12,4) NOT NULL DEFAULT 0,
    total_cost DECIMAL(14,4) NOT NULL DEFAULT 0,
    remarks VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_ledger_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_inventory_ledger_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);
