CREATE TABLE IF NOT EXISTS roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nickname VARCHAR(50) NOT NULL,
  name VARCHAR(50) NOT NULL,
  furigana VARCHAR(50) NOT NULL,
  postal_code VARCHAR(7) NOT NULL,
  address VARCHAR(255) NOT NULL,
  phone_number VARCHAR(11) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role_id INT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (role_id) REFERENCES roles(id)
  
);

-- 初期ロール
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');


-- =========================================
-- インテリア雑貨EC用 schema.sql
-- 要件：カート＋セット割（数量3以上で10%OFF）＋在庫ログ＋レビュー＋売上ランキング（直近1か月）＋残数表示
-- =========================================




-- -------------------------
-- products：商品マスタ（在庫は持たず、在庫ログから集計する）
-- -------------------------
CREATE TABLE IF NOT EXISTS products(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(200) NOT NULL,
	price INT NOT NULL,
	description TEXT NOT NULL,
	category VARCHAR(100) NOT NULL,
	image_name VARCHAR(255),
	is_active TINYINT(1) NOT NULL DEFAULT 1,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- -------------------------
-- product_inventory_logs：在庫の正本（増減履歴）
-- 残数表示 = SUM(delta_qty)
-- 入荷 + / 販売 - / 返品 + / 棚卸 ±
-- -------------------------
CREATE TABLE IF NOT EXISTS product_inventory_logs(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	product_id INT NOT NULL,
	delta_qty INT NOT NULL, --在庫の増減量
	reason VARCHAR(50) NOT NULL,
	related_order_id INT NOT NULL DEFAULT 0, -- 修正（SQL）: NULL禁止、初期は0固定
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	UNIQUE KEY uq_inventory (product_id, reason, related_order_id), -- 修正（SQL）
	FOREIGN KEY (product_id) REFERENCES products (id)
);


-- -------------------------
-- cart_items：カート（同一ユーザー×同一商品は1行にまとめる）
-- -------------------------
CREATE TABLE IF NOT EXISTS cart_items(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	user_id INT NOT NULL,
	product_id INT NOT NULL,
	quantity INT NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	UNIQUE KEY uq_cart_user_product (user_id, product_id),
	FOREIGN KEY (user_id) REFERENCES users (id),
	FOREIGN KEY (product_id) REFERENCES products (id)
);


-- -------------------------
-- orders：注文ヘッダ（確定金額を保存）
-- status例：CREATED / PAID / CANCELED
-- subtotal：割引前合計、discount：割引額、total：最終支払額
-- -------------------------
CREATE TABLE IF NOT EXISTS orders(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	user_id INT NOT NULL,
	status VARCHAR(20) NOT NULL,
	subtotal INT NOT NULL,
	discount INT NOT NULL DEFAULT 0,
	total INT NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	paid_at DATETIME,
	FOREIGN KEY (user_id) REFERENCES users (id)
);


-- -------------------------
-- order_items：注文明細（購入時点の単価を保存。ランキング集計はここ）
-- line_total = unit_price * quantity
-- -------------------------
CREATE TABLE IF NOT EXISTS order_items(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	order_id INT NOT NULL,
	product_id INT NOT NULL,
	unit_price INT NOT NULL,
	quantity INT NOT NULL,
	line_total INT NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (order_id) REFERENCES orders (id),
	FOREIGN KEY (product_id) REFERENCES products (id) 
);


-- -------------------------
-- reviews：レビュー（1ユーザー1商品1レビュー）
-- rating：1〜5（DBでCHECKが効かない場合があるのでアプリ側でも制限）
-- -------------------------
CREATE TABLE IF NOT EXISTS reviews(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	user_id INT NOT NULL,
	product_id INT NOT NULL,
	rating INT NOT NULL,
	comment TEXT,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	UNIQUE KEY uq_review_user_product (user_id, product_id),
	FOREIGN KEY (user_id) REFERENCES users (id),
	FOREIGN KEY (product_id) REFERENCES products (id)
);


-- -------------------------
-- discount_rules：割引ルール（今回は1ルールだけ運用）
-- 数量3以上で10%OFF：min_qty=3, percent_off=10
-- rule_typeは識別子（コード側で分岐に使う）
-- -------------------------
CREATE TABLE IF NOT EXISTS discount_rules(
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	rule_type VARCHAR(50) NOT NULL,
	min_qty INT NOT NULL,
	percent_off INT NOT NULL,
	is_active TINYINT(1) NOT NULL DEFAULT 1,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- -------------------------
-- インデックス：集計（在庫/ランキング）を速くする
-- -------------------------
--CREATE INDEX idx_inventory_product_created ON product_inventory_logs (product_id, created_at);
--CREATE INDEX idx_orders_paid_at ON orders (paid_at);
--CREATE INDEX idx_order_items_product ON order_items (product_id);
--CREATE INDEX idx_order_items_order ON order_items (order_id);


-- -------------------------
-- 初期データ：割引ルール（数量3以上で10%OFF）
-- ※すでに入ってたら重複するので、運用で1回だけ実行する想定
-- -------------------------
INSERT INTO discount_rules (rule_type, min_qty, percent_off, is_active)
VALUES ('CART_QTY_GTE_PERCENT_OFF', 3, 10, 1);

-- 修正（SQL）：お気に入り（ログイン後はDBへ永続化、ログイン前はSessionで保持）
CREATE TABLE IF NOT EXISTS favorites (
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_favorites_user_product (user_id, product_id),
  CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products(id)
);
