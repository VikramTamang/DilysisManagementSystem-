CREATE TABLE merchants (
                           id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name          VARCHAR(255)        NOT NULL,
                           email         VARCHAR(255)        NOT NULL UNIQUE,
                           password      VARCHAR(255)        NOT NULL,
                           phone         VARCHAR(50)         NOT NULL,
                           business_name VARCHAR(255)        NOT NULL,
                           role          VARCHAR(50)         NOT NULL,
                           status        VARCHAR(50)         NOT NULL,
                           created_at    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);