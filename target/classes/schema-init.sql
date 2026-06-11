-- ─────────────────────────────────────────────────────────────────────────────
-- Wallet API – MySQL Schema
-- Run this once to create the database before starting the application.
-- Hibernate (ddl-auto=update) will create/update tables automatically,
-- but you still need the database itself to exist first.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS walletdb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE walletdb;

-- Optional: create a dedicated app user instead of using root
-- CREATE USER IF NOT EXISTS 'wallet_user'@'localhost' IDENTIFIED BY 'wallet_pass';
-- GRANT ALL PRIVILEGES ON walletdb.* TO 'wallet_user'@'localhost';
-- FLUSH PRIVILEGES;
