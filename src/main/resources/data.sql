-- =========================================
-- data.sql（初期データ）
-- 目的：カテゴリ連動（category_id） + 表示確認
-- =========================================

-- -------------------------
-- categories：先に投入（FKの親）
-- -------------------------
INSERT IGNORE INTO categories (name, sort_order) VALUES
('アート・雑貨', 1),
('ソファ・椅子', 2),
('収納家具', 3),
('キッチン収納・食器棚', 4),
('ごみ箱・玄関小物', 5),
('ライト・照明', 6),
('ラグ・カーペット', 7),
('クッション', 8),
('時計', 9),
('花器・プランター・グリーン', 10),
('キッチン家電・キッチン用品', 11),
('デザイン家電・オーディオ', 12);

-- =========================================
-- 修正（SQL）: ダミー商品（カテゴリ連動・画像はNOIMAGE固定）
--「子→親」の順でDELETEが必要
-- =========================================
DELETE FROM favorites WHERE product_id >= 6;
DELETE FROM cart_items WHERE product_id >= 6;
DELETE FROM product_inventory_logs WHERE product_id >= 6;

-- -------------------------
-- products：初期商品（1-5）
-- category_idをJOINで埋める（category文字列も互換で残す）
-- -------------------------
INSERT INTO products (id, name, price, description, category, category_id, image_name, is_active)
SELECT v.id, v.name, v.price, v.description, v.category, c.id, v.image_name, v.is_active
FROM (
	SELECT 1 AS id, 'ウッドフレーム（A4）' AS name, 2980 AS price, '天然木のA4フレーム。' AS description, 'アート・雑貨' AS category, 'frame_a4.jpg' AS image_name, 1 AS is_active
	UNION ALL SELECT 2, 'ガラス花瓶（S）', 1980, '小さめの透明ガラス花瓶。', '花器・プランター・グリーン', 'vase_s.jpg', 1
	UNION ALL SELECT 3, 'テーブルランプ（LED）', 7980, '暖色LEDのテーブルランプ。', 'ライト・照明', 'lamp_led.jpg', 1
	UNION ALL SELECT 4, 'ラタン収納バスケット', 4980, 'ラタン素材の収納バスケット。', '収納家具', 'basket_rattan.jpg', 1
	UNION ALL SELECT 5, 'クッションカバー（リネン）', 2480, 'リネン素材のクッションカバー。', 'クッション', 'cushion_linen.jpg', 1
) v
LEFT JOIN categories c ON c.name = v.category
ON DUPLICATE KEY UPDATE
	name = VALUES(name),
	price = VALUES(price),
	description = VALUES(description),
	category = VALUES(category),
	category_id = VALUES(category_id),
	image_name = VALUES(image_name),
	is_active = VALUES(is_active);

-- -------------------------
-- products：ダミー商品（6-35）
-- category_idをJOINで埋める（category文字列も互換で残す）
-- -------------------------
INSERT INTO products (id, name, price, description, category, category_id, image_name, is_active)
SELECT v.id, v.name, v.price, v.description, v.category, c.id, v.image_name, v.is_active
FROM (
	SELECT 6 AS id,  '北欧風 2人掛けソファ（ファブリック）' AS name, 19800 AS price, 'リビングの主役になる2人掛けソファ。' AS description, 'ソファ・椅子' AS category, 'noimage.png' AS image_name, 1 AS is_active
	UNION ALL SELECT 7,  'カウチソファ（ライトグレー）', 17900, '省スペースでも置けるカウチタイプ。', 'ソファ・椅子', 'noimage.png', 1
	UNION ALL SELECT 8,  'ダイニングチェア（オーク）', 12800, '木目がきれいな定番チェア。', 'ソファ・椅子', 'noimage.png', 1
	UNION ALL SELECT 9,  'スタッキングスツール（ナチュラル）', 5900, '重ねて収納できるスツール。', 'ソファ・椅子', 'noimage.png', 1
	UNION ALL SELECT 10, 'チェスト（3段）', 14900, '小物をすっきり整理できる3段チェスト。', '収納家具', 'noimage.png', 1
	UNION ALL SELECT 11, 'オープンシェルフ（5段）', 13900, '見せる収納に便利なオープン棚。', '収納家具', 'noimage.png', 1
	UNION ALL SELECT 12, 'サイドボード（幅90cm）', 18900, 'リビングの収納にちょうどいい。', '収納家具', 'noimage.png', 1
	UNION ALL SELECT 13, 'キッチンボード（幅120cm）', 19900, '食器をまとめて収納できるキッチンボード。', 'キッチン収納・食器棚', 'noimage.png', 1
	UNION ALL SELECT 14, 'カップボード（ロータイプ）', 16800, '圧迫感の少ないロータイプ。', 'キッチン収納・食器棚', 'noimage.png', 1
	UNION ALL SELECT 15, 'ペダル式ゴミ箱（20L）', 4200, 'フタ付きでニオイ対策にも。', 'ごみ箱・玄関小物', 'noimage.png', 1
	UNION ALL SELECT 16, 'スリッパラック（省スペース）', 3600, '玄関をすっきり見せるラック。', 'ごみ箱・玄関小物', 'noimage.png', 1
	UNION ALL SELECT 17, 'テーブルライト（調光LED）', 7800, '読書灯にちょうどいい調光ライト。', 'ライト・照明', 'noimage.png', 1
	UNION ALL SELECT 18, 'フロアライト（スリム）', 15900, '間接照明として使いやすい。', 'ライト・照明', 'noimage.png', 1
	UNION ALL SELECT 19, 'ペンダントライト（ガラス）', 12900, '食卓に映えるガラスシェード。', 'ライト・照明', 'noimage.png', 1
	UNION ALL SELECT 20, 'ラグ（北欧柄 140×200）', 19800, '季節を問わず使える北欧柄。', 'ラグ・カーペット', 'noimage.png', 1
	UNION ALL SELECT 21, 'シャギーラグ（円形）', 9900, '足元がふわっと心地いい。', 'ラグ・カーペット', 'noimage.png', 1
	UNION ALL SELECT 22, 'キッチンマット（45×180）', 2900, '汚れに強いキッチンマット。', 'ラグ・カーペット', 'noimage.png', 1
	UNION ALL SELECT 23, 'クッション（45×45）', 2500, 'ソファに合わせやすい定番サイズ。', 'クッション', 'noimage.png', 1
	UNION ALL SELECT 24, 'クッションカバー（リネン）', 2400, '肌触りの良いリネン素材。', 'クッション', 'noimage.png', 1
	UNION ALL SELECT 25, '置き時計（シンプル）', 3900, '見やすい文字盤の置き時計。', '時計', 'noimage.png', 1
	UNION ALL SELECT 26, '壁掛け時計（静音）', 5200, 'カチカチ音が気になりにくい。', '時計', 'noimage.png', 1
	UNION ALL SELECT 27, '花瓶（ガラス S）', 1800, '一輪挿しにちょうどいいサイズ。', '花器・プランター・グリーン', 'noimage.png', 1
	UNION ALL SELECT 28, 'プランター（陶器）', 3200, '観葉植物に合う陶器プランター。', '花器・プランター・グリーン', 'noimage.png', 1
	UNION ALL SELECT 29, '電気ケトル（1.0L）', 6900, '必要な分だけすぐ沸かせる。', 'キッチン家電・キッチン用品', 'noimage.png', 1
	UNION ALL SELECT 30, 'トースター（2枚焼き）', 9800, '毎朝のトーストが手軽に。', 'キッチン家電・キッチン用品', 'noimage.png', 1
	UNION ALL SELECT 31, 'キッチンツールセット（5点）', 2400, '最低限そろうツールセット。', 'キッチン家電・キッチン用品', 'noimage.png', 1
	UNION ALL SELECT 32, 'Bluetoothスピーカー（コンパクト）', 8900, 'デスクに置ける小型スピーカー。', 'デザイン家電・オーディオ', 'noimage.png', 1
	UNION ALL SELECT 33, 'デスクライト（USB給電）', 4200, '在宅ワーク向けUSBライト。', 'デザイン家電・オーディオ', 'noimage.png', 1
	UNION ALL SELECT 34, 'ミニ加湿器（卓上）', 3500, '乾燥が気になる季節に。', 'デザイン家電・オーディオ', 'noimage.png', 1
	UNION ALL SELECT 35, 'アロマディフューザー（超音波）', 5200, '香りでリラックスできる。', 'デザイン家電・オーディオ', 'noimage.png', 1
) v
JOIN categories c ON c.name = v.category
ON DUPLICATE KEY UPDATE
	name = VALUES(name),
	price = VALUES(price),
	description = VALUES(description),
	category = VALUES(category),
	category_id = VALUES(category_id),
	image_name = VALUES(image_name),
	is_active = VALUES(is_active);

-- -------------------------
-- product_inventory_logs：入荷（1-5）
-- -------------------------
INSERT IGNORE INTO product_inventory_logs (product_id, delta_qty, reason, related_order_id)
VALUES
  (1, 20, 'IN', 0),
  (2, 20, 'IN', 0),
  (3, 20, 'IN', 0),
  (4, 20, 'IN', 0),
  (5, 20, 'IN', 0);

-- 在庫（6-35）
INSERT IGNORE INTO product_inventory_logs (product_id, delta_qty, reason, related_order_id)
VALUES
  (6,  12, 'IN_SEED', 0),(7,  8,  'IN_SEED', 0),(8,  15, 'IN_SEED', 0),(9,  10, 'IN_SEED', 0),
  (10, 20, 'IN_SEED', 0),(11, 14, 'IN_SEED', 0),(12,  9, 'IN_SEED', 0),(13,  7, 'IN_SEED', 0),
  (14, 11, 'IN_SEED', 0),(15, 16, 'IN_SEED', 0),(16, 13, 'IN_SEED', 0),(17, 18, 'IN_SEED', 0),
  (18,  6, 'IN_SEED', 0),(19,  5, 'IN_SEED', 0),(20, 10, 'IN_SEED', 0),(21,  8, 'IN_SEED', 0),
  (22, 12, 'IN_SEED', 0),(23, 25, 'IN_SEED', 0),(24, 19, 'IN_SEED', 0),(25,  7, 'IN_SEED', 0),
  (26,  9, 'IN_SEED', 0),(27, 14, 'IN_SEED', 0),(28, 10, 'IN_SEED', 0),(29, 16, 'IN_SEED', 0),
  (30, 11, 'IN_SEED', 0),(31, 20, 'IN_SEED', 0),(32,  8, 'IN_SEED', 0),(33, 13, 'IN_SEED', 0),
  (34,  9, 'IN_SEED', 0),(35,  6, 'IN_SEED', 0);