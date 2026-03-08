DROP TABLE IF EXISTS transfer;
DROP TABLE IF EXISTS account;

CREATE TABLE account (
    account_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    budget DOUBLE PRECISION NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE'
);

CREATE TABLE transfer (
    transfer_id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (sender_id) REFERENCES account(account_id),
    FOREIGN KEY (receiver_id) REFERENCES account(account_id)
);

INSERT INTO account (name, budget, status) VALUES
    ('John Doe1', 1000.00, 'ACTIVE'),
    ('John Doe2', 500.00, 'ACTIVE'),
    ('John Doe3', 2000.00, 'ACTIVE'),
    ('John Doe4', 0.00, 'INACTIVE');
