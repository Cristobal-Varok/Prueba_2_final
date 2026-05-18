CREATE TABLE tickets (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL,
    admin_response VARCHAR(2000),

    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);