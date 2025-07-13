# --- !Ups

-- Tabel kategori
CREATE TABLE kategori
(
    kategori_id SERIAL PRIMARY KEY,
    nama        VARCHAR(100) NOT NULL,
    is_delete   BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Tabel barang
CREATE TABLE barang
(
    barang_id   SERIAL PRIMARY KEY,
    nama        VARCHAR(100) NOT NULL,
    kategori_id INTEGER      NOT NULL REFERENCES kategori (kategori_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    harga       DOUBLE PRECISION,
    stok        INTEGER,
    is_delete   BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Seed data kategori
INSERT INTO kategori (nama)
VALUES ('Elektronik'),
       ('Pakaian'),
       ('Makanan'),
       ('Peralatan Rumah Tangga');

-- Seed data barang
INSERT INTO barang (nama, kategori_id, harga, stok)
VALUES ('Laptop ASUS ROG', 1, 18000000.00, 10),
       ('Smartphone Samsung', 1, 7500000.00, 25),
       ('Kaos Polos Hitam', 2, 75000.00, 100),
       ('Celana Jeans', 2, 150000.00, 60),
       ('Mie Instan', 3, 3000.00, 500),
       ('Susu Kental Manis', 3, 12000.00, 200),
       ('Vacuum Cleaner', 4, 850000.00, 15),
       ('Dispenser Air', 4, 450000.00, 20);

# --- !Downs
DROP TABLE IF EXISTS barang;
DROP TABLE IF EXISTS kategori;
