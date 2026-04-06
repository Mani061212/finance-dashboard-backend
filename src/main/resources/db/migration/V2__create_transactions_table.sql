CREATE TABLE transactions (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount           NUMERIC(15,2) NOT NULL,
    type             VARCHAR(10)   NOT NULL,
    category         VARCHAR(50)   NOT NULL,
    description      TEXT,
    transaction_date DATE          NOT NULL,
    is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_type   CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_amount CHECK (amount > 0)
);

CREATE INDEX idx_tx_user_id      ON transactions(user_id);
CREATE INDEX idx_tx_type         ON transactions(type);
CREATE INDEX idx_tx_category     ON transactions(category);
CREATE INDEX idx_tx_date         ON transactions(transaction_date);
CREATE INDEX idx_tx_deleted      ON transactions(is_deleted);
CREATE INDEX idx_tx_user_deleted ON transactions(user_id, is_deleted);