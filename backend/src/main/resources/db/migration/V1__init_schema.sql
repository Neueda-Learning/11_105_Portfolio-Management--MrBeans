-- V1: Initialize schema for Portfolio Manager
-- All primary keys are UUIDs mapped to CHAR(36)
-- Money fields use DECIMAL (precision 20, scale 4 for prices/amounts, scale 8 for rates/quantities)

-- 1. investments
CREATE TABLE investments (
    id CHAR(36) PRIMARY KEY,
    symbol VARCHAR(100) NOT NULL,
    name VARCHAR(255),
    type VARCHAR(50) NOT NULL, -- STOCK, BOND, CASH, OTHER
    currency VARCHAR(10) NOT NULL,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. transactions
CREATE TABLE transactions (
    id CHAR(36) PRIMARY KEY,
    investment_id CHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL, -- BUY, SELL, DEPOSIT, WITHDRAWAL
    quantity DECIMAL(20, 8),
    price DECIMAL(20, 4),
    currency VARCHAR(10),
    fx_rate_to_home DECIMAL(20, 8),
    txn_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_investment FOREIGN KEY (investment_id) REFERENCES investments(id)
);

-- Index per PRD Section 5
CREATE INDEX idx_transactions_inv_date ON transactions(investment_id, txn_date);

-- 3. price_snapshots
CREATE TABLE price_snapshots (
    id CHAR(36) PRIMARY KEY,
    investment_id CHAR(36) NOT NULL,
    price DECIMAL(20, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    fetched_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_price_snapshots_investment FOREIGN KEY (investment_id) REFERENCES investments(id)
);

-- Index per PRD Section 5
CREATE INDEX idx_price_snapshots_inv_fetched ON price_snapshots(investment_id, fetched_at);

-- 4. fx_rates
CREATE TABLE fx_rates (
    id CHAR(36) PRIMARY KEY,
    from_currency VARCHAR(10) NOT NULL,
    to_currency VARCHAR(10) NOT NULL,
    rate DECIMAL(20, 8) NOT NULL,
    rate_date DATE NOT NULL
);

-- Unique constraint index per PRD Section 5
CREATE UNIQUE INDEX uk_fx_rates_from_to_date ON fx_rates(from_currency, to_currency, rate_date);

-- 5. dividends
CREATE TABLE dividends (
    id CHAR(36) PRIMARY KEY,
    investment_id CHAR(36) NOT NULL,
    amount DECIMAL(20, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    mode VARCHAR(50) NOT NULL, -- DISTRIBUTIVE, ACCUMULATIVE
    ex_date DATE,
    payment_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dividends_investment FOREIGN KEY (investment_id) REFERENCES investments(id)
);

CREATE INDEX idx_dividends_investment ON dividends(investment_id);

-- 6. user_settings
CREATE TABLE user_settings (
    id CHAR(36) PRIMARY KEY,
    home_currency VARCHAR(10) NOT NULL,
    update_frequency VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
