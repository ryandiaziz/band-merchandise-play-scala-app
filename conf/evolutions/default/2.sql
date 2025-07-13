# --- !Ups
CREATE TABLE users
(
    user_id  SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL
);

-- Users
INSERT INTO users (user_id, username)
VALUES (1, 'user1'),
       (2, 'user2'),
       (3, 'admin'),
       (4, 'guest');

CREATE TABLE keranjang
(
    keranjang_id SERIAL PRIMARY KEY,
    user_id      INT              NOT NULL REFERENCES users (user_id),
    total_harga  DOUBLE PRECISION NOT NULL,
    is_delete    BOOLEAN DEFAULT TRUE
);

CREATE TABLE item_keranjang
(
    item_keranjang_id SERIAL PRIMARY KEY,
    keranjang_id      INT              NOT NULL REFERENCES keranjang (keranjang_id),
    barang_id         INT              NOT NULL REFERENCES barang (barang_id),
    jumlah            DOUBLE PRECISION NOT NULL,
    unit_harga        DOUBLE PRECISION NOT NULL,
    total_harga_item  DOUBLE PRECISION NOT NULL
);

-- Keranjang
INSERT INTO keranjang (keranjang_id, user_id, total_harga)
VALUES (1, 1, 20000),
       (2, 2, 10000),
       (3, 3, 0);

-- Item Keranjang
INSERT INTO item_keranjang (item_keranjang_id, keranjang_id, barang_id, jumlah, unit_harga, total_harga_item)
VALUES (1, 1, 1, 2, 5000, 10000),
       (2, 1, 2, 1, 10000, 10000),
       (3, 2, 3, 1, 7000, 7000),
       (4, 2, 5, 1, 4000, 4000),
       (5, 3, 4, 3, 3000, 9000);

# --- !Downs

DELETE
FROM item_keranjang;
DELETE
FROM keranjang;
DELETE
FROM users;