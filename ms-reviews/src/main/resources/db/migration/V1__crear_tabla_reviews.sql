CREATE TABLE IF NOT EXISTS reviews (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(255) NOT NULL,
    product_id   VARCHAR(255) NOT NULL,
    rating       INT          NOT NULL,
    comment      VARCHAR(1000) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    product_type VARCHAR(50)  NOT NULL,

    -- Índices para búsquedas rápidas
    INDEX idx_product_id (product_id),
    INDEX idx_username (username),
    INDEX idx_product_type (product_type),

    -- Un usuario no puede reseñar el mismo producto dos veces
    CONSTRAINT unique_user_product UNIQUE (username, product_id)
);