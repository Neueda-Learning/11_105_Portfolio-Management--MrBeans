-- V2: Seed single default user_settings row so the app never queries an empty settings table
INSERT INTO user_settings (id, home_currency, update_frequency) 
VALUES (UUID(), 'INR', 'daily');
