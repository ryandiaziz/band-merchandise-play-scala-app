--- !Ups
-- Ini adalah script migrasi untuk membuat tabel

CREATE TABLE merch_type
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_delete   BOOLEAN   DEFAULT FALSE
);

CREATE TABLE merchandise
(
    id            SERIAL PRIMARY KEY,
    title         VARCHAR(255)   NOT NULL,
    band_name     VARCHAR(255)   NOT NULL,
    merch_type_id INT            NOT NULL,
    description   TEXT,
    price         DECIMAL(10, 2) NOT NULL,
    image_url     VARCHAR(255),
    stock         INT            NOT NULL DEFAULT 0,
    created_at    TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    is_delete     BOOLEAN                 DEFAULT FALSE,
    FOREIGN KEY (merch_type_id) REFERENCES merch_type (id)
);

CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    city_id    INT, -- Asumsi ada tabel city, jika tidak ini bisa VARCHAR atau dihapus
    address    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_delete  BOOLEAN   DEFAULT FALSE
);

CREATE TABLE cart
(
    id         SERIAL PRIMARY KEY,
    user_id    INT            NOT NULL,
    price      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status     VARCHAR(50)    NOT NULL DEFAULT 'active', -- 'active', 'ordered', 'cancelled'
    created_at TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    is_delete  BOOLEAN                 DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE cart_merch
(
    id             SERIAL PRIMARY KEY,
    cart_id        INT            NOT NULL,
    merchandise_id INT            NOT NULL,
    qty            INT            NOT NULL,
    unit_price     DECIMAL(10, 2) NOT NULL,
    total_price    DECIMAL(10, 2) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_delete      BOOLEAN   DEFAULT FALSE,
    FOREIGN KEY (cart_id) REFERENCES cart (id),
    FOREIGN KEY (merchandise_id) REFERENCES merchandise (id)
);

CREATE TABLE transactions
(
    id                     SERIAL PRIMARY KEY,
    cart_id                INT            NOT NULL UNIQUE, -- Setiap transaksi terkait satu keranjang unik
    cart_price             DECIMAL(10, 2) NOT NULL,
    delivery_service_price DECIMAL(10, 2) NOT NULL,
    total_price            DECIMAL(10, 2) NOT NULL,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_delete              BOOLEAN   DEFAULT FALSE,
    FOREIGN KEY (cart_id) REFERENCES cart (id)
);

--- !Downs
-- Ini adalah script migrasi untuk menghapus tabel (urutan terbalik dari pembuatan)

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS cart_merch;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS merchandise;
DROP TABLE IF EXISTS merch_type;