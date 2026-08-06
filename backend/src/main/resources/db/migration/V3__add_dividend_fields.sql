-- V3: Enhance dividends table with per-share tracking, withholding tax, and reinvestment price

ALTER TABLE dividends
    ADD COLUMN dividend_per_share DECIMAL(20, 8) NULL AFTER amount,
    ADD COLUMN withholding_tax    DECIMAL(20, 4) NOT NULL DEFAULT 0.0000 AFTER currency,
    ADD COLUMN reinvestment_price DECIMAL(20, 4) NULL AFTER withholding_tax;
