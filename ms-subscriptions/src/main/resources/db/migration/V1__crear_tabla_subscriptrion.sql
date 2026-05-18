CREATE TABLE subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(255) NOT NULL UNIQUE,

    type VARCHAR(50) NOT NULL,

    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    end_date DATETIME NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    cancelled_at DATETIME NULL,

    INDEX idx_username (username),
    INDEX idx_active (active),
    INDEX idx_type (type),
    INDEX idx_end_date (end_date)
);