CREATE TABLE IF NOT EXISTS customers (
    customer_id   TEXT PRIMARY KEY,
    first_name    TEXT NOT NULL,
    surname       TEXT NOT NULL,
    address       TEXT NOT NULL,
    phone         TEXT NOT NULL,
    email         TEXT NOT NULL,
    username      TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number  TEXT PRIMARY KEY,
    customer_id     TEXT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    account_type    TEXT NOT NULL CHECK (account_type IN ('SAVINGS','INVESTMENT','CHEQUE')),
    branch          TEXT NOT NULL,
    opening_date    TEXT NOT NULL,
    balance         REAL NOT NULL DEFAULT 0,
    status          TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CLOSED')),
    employer_name   TEXT,
    company_address TEXT
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id TEXT PRIMARY KEY,
    account_number TEXT NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,
    txn_date       TEXT NOT NULL,
    type           TEXT NOT NULL CHECK (type IN ('DEPOSIT','WITHDRAWAL','INTEREST')),
    amount         REAL NOT NULL,
    description    TEXT,
    balance_after  REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS admins (
    admin_id      TEXT PRIMARY KEY,
    name          TEXT NOT NULL,
    username      TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL
);
