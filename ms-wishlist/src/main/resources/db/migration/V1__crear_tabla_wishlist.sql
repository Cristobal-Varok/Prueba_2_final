CREATE TABLE wishlist (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    product_price DOUBLE NULL,
    image_url VARCHAR(500) NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT unique_user_product
        UNIQUE (username, product_id),

    INDEX idx_username (username),
    INDEX idx_product_id (product_id),
    INDEX idx_product_type (product_type),
    INDEX idx_added_at (added_at)
);