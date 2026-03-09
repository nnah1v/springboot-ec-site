package com.natsuka.ec.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.natsuka.ec.dto.SessionCart;
import com.natsuka.ec.entity.CartItem;
import com.natsuka.ec.entity.DiscountRule;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.repository.CartItemRepository;
import com.natsuka.ec.repository.DiscountRuleRepository;
import com.natsuka.ec.repository.ProductRepository;

//カート機能の業務ロジックを担当するService
// Controllerから呼ばれて、DB操作・在庫確認・割引計算をまとめて行う
@Service
public class CartService {

	//DBのcart_itemsテーブル操作用
	private final CartItemRepository cartItemRepository;

	//商品情報取得用（price取得、商品存在確認）
	private final ProductRepository productRepository;

	//割引ルール取得用
	private final DiscountRuleRepository discountRuleRepository;

	//在庫数確認用
	private final InventoryService inventoryService;

	//コンストラクタインジェクション
	public CartService(
			CartItemRepository cartItemRepository,
			ProductRepository productRepository,
			DiscountRuleRepository discountRuleRepository,
			InventoryService inventoryService) {

		this.cartItemRepository = cartItemRepository;
		this.productRepository = productRepository;
		this.discountRuleRepository = discountRuleRepository;
		this.inventoryService = inventoryService;
	}

	//ログインユーザーのカート一覧を新しい更新順で取得
	public List<CartItem> findCartItems(Integer userId) {
		return cartItemRepository.findByUserIdOrderByUpdatedAtDesc(userId);
	}

	//DBカートの合計金額を計算
	// 各行の「商品価格 × 数量」を足し合わせる
	public int calculateTotalAmount(Integer userId) {
		List<CartItem> cartItems = findCartItems(userId);
		int totalAmount = 0;

		for (CartItem cartItem : cartItems) {
			totalAmount += cartItem.getProduct().getPrice() * cartItem.getQuantity();
		}

		return totalAmount;
	}

	//DBカートの割引金額を計算
	// CART_AMOUNT_GTE_PERCENT_OFF というルールが有効なら適用する
	public int calculateDiscountAmount(Integer userId) {
		int totalAmount = calculateTotalAmount(userId);

		//有効な割引ルールを1件取得
		DiscountRule discountRule = discountRuleRepository
				.findFirstByRuleTypeAndIsActiveTrue("CART_AMOUNT_GTE_PERCENT_OFF");

		//ルール未登録なら割引なし
		if (discountRule == null) {
			return 0;
		}

		//合計金額が条件以上なら割引率を適用
		if (totalAmount >= discountRule.getMinAmount()) {
			return totalAmount * discountRule.getPercentOff() / 100;
		}

		return 0;
	}

	//DBカートの最終金額 = 合計 - 割引
	public int calculateFinalAmount(Integer userId) {
		int totalAmount = calculateTotalAmount(userId);
		int discountAmount = calculateDiscountAmount(userId);

		return totalAmount - discountAmount;
	}

	//DBカートへ商品追加
	// 既に同じ商品が入っていれば数量加算、なければ新規追加
	@Transactional
	public void addCartItem(Integer userId, Integer productId, Integer addQuantity) {

		//不正な入力値チェック
		if (userId == null || productId == null || addQuantity == null || addQuantity <= 0) {
			throw new IllegalStateException("数量が正しくありません。");
		}

		//現在の在庫数を取得
		Integer availableStock = inventoryService.getAvailableStock(productId);

		//在庫ゼロなら追加不可
		if (availableStock <= 0) {
			throw new IllegalStateException("在庫がありません。");
		}

		//既にカートに同じ商品があるか確認
		Optional<CartItem> existing = cartItemRepository.findByUserIdAndProduct_Id(userId, productId);

		if (existing.isPresent()) {
			CartItem cartItem = existing.get();

			//今の数量 + 今回追加分 の合計で在庫チェック
			int requestedQuantity = cartItem.getQuantity() + addQuantity;

			if (requestedQuantity > availableStock) {
				throw new IllegalStateException(
						"在庫が不足しています。現在の在庫は " + availableStock + " 個です。");
			}

			//既存行の数量だけ更新
			cartItem.setQuantity(requestedQuantity);
			cartItemRepository.save(cartItem);
			return;
		}

		//新規追加時も在庫チェック
		if (addQuantity > availableStock) {
			throw new IllegalStateException(
					"在庫が不足しています。現在の在庫は " + availableStock + " 個です。");
		}

		//商品存在確認
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalStateException("商品が見つかりません。"));

		//新しいカート行を作成して保存
		CartItem cartItem = new CartItem();
		cartItem.setUserId(userId);
		cartItem.setProduct(product);
		cartItem.setQuantity(addQuantity);
		cartItemRepository.save(cartItem);
	}

	//DBカートの数量更新
	// 0以下なら削除扱いにする
	@Transactional
	public void updateCartItemQuantity(Integer userId, Integer productId, Integer newQuantity) {

		//nullチェック
		if (userId == null || productId == null || newQuantity == null) {
			throw new IllegalStateException("数量が正しくありません。");
		}

		//0以下は削除に寄せる
		if (newQuantity <= 0) {
			removeCartItem(userId, productId);
			return;
		}

		//在庫確認
		Integer availableStock = inventoryService.getAvailableStock(productId);

		if (availableStock <= 0) {
			throw new IllegalStateException("在庫がありません。");
		}

		//更新後数量が在庫を超えるならエラー
		if (newQuantity > availableStock) {
			throw new IllegalStateException(
					"在庫が不足しています。現在の在庫は " + availableStock + " 個です。");
		}

		//対象の商品がカートに存在する時だけ更新
		cartItemRepository.findByUserIdAndProduct_Id(userId, productId).ifPresent(cartItem -> {
			cartItem.setQuantity(newQuantity);
			cartItemRepository.save(cartItem);
		});
	}

	//DBカートから1商品削除
	@Transactional
	public void removeCartItem(Integer userId, Integer productId) {
		if (userId == null || productId == null) {
			return;
		}

		cartItemRepository.findByUserIdAndProduct_Id(userId, productId)
				.ifPresent(cartItemRepository::delete);
	}

	// =====================================================
	// ログイン前（SessionCart）
	// =====================================================

	//セッションカートへ商品追加
	// DBではなくメモリ上のSessionCartに追加する
	public void addToSessionCart(SessionCart sessionCart, Integer productId, Integer addQuantity) {

		//入力値チェック
		if (sessionCart == null || productId == null || addQuantity == null || addQuantity <= 0) {
			throw new IllegalStateException("数量が正しくありません。");
		}

		//セッションカートでも在庫チェックは必須
		Integer availableStock = inventoryService.getAvailableStock(productId);

		if (availableStock <= 0) {
			throw new IllegalStateException("在庫がありません。");
		}

		//今すでにセッションカートに入っている数量を取得
		int currentQuantity = sessionCart.asUnmodifiableMap().getOrDefault(productId, 0);

		//追加後数量を計算
		int requestedQuantity = currentQuantity + addQuantity;

		//追加後数量が在庫超過ならエラー
		if (requestedQuantity > availableStock) {
			throw new IllegalStateException(
					"在庫が不足しています。現在の在庫は " + availableStock + " 個です。");
		}

		//セッションカートへ加算
		sessionCart.add(productId, addQuantity);
	}

	//セッションカートの数量更新
	// 0以下なら削除にする
	public void updateSessionCart(SessionCart sessionCart, Integer productId, Integer newQuantity) {

		//入力値チェック
		if (sessionCart == null || productId == null || newQuantity == null) {
			throw new IllegalStateException("数量が正しくありません。");
		}

		//0以下なら削除
		if (newQuantity <= 0) {
			sessionCart.remove(productId);
			return;
		}

		//在庫確認
		Integer availableStock = inventoryService.getAvailableStock(productId);

		if (availableStock <= 0) {
			throw new IllegalStateException("在庫がありません。");
		}

		//指定数量が在庫超過ならエラー
		if (newQuantity > availableStock) {
			throw new IllegalStateException(
					"在庫が不足しています。現在の在庫は " + availableStock + " 個です。");
		}

		//セッションカート数量更新
		sessionCart.update(productId, newQuantity);
	}

	//セッションカートから商品削除
	public void removeFromSessionCart(SessionCart sessionCart, Integer productId) {
		if (sessionCart == null) {
			return;
		}
		sessionCart.remove(productId);
	}

	//セッションカートの合計金額計算
	public int calculateSessionTotalAmount(SessionCart sessionCart) {
		if (sessionCart == null || sessionCart.isEmpty()) {
			return 0;
		}

		int totalAmount = 0;

		//Map<productId, quantity> を順番に処理
		for (Map.Entry<Integer, Integer> entry : sessionCart.asUnmodifiableMap().entrySet()) {
			Integer productId = entry.getKey();
			Integer quantity = entry.getValue();

			//商品価格取得
			Product product = productRepository.findById(productId).orElse(null);
			if (product == null) {
				continue;
			}

			totalAmount += product.getPrice() * quantity;
		}

		return totalAmount;
	}

	//セッションカートの割引金額計算
	public int calculateSessionDiscountAmount(SessionCart sessionCart) {
		int totalAmount = calculateSessionTotalAmount(sessionCart);

		//DBカートと同じルールを使う
		DiscountRule discountRule = discountRuleRepository
				.findFirstByRuleTypeAndIsActiveTrue("CART_AMOUNT_GTE_PERCENT_OFF");

		if (discountRule == null) {
			return 0;
		}

		if (totalAmount >= discountRule.getMinAmount()) {
			return totalAmount * discountRule.getPercentOff() / 100;
		}

		return 0;
	}

	//セッションカートの最終金額 = 合計 - 割引
	public int calculateSessionFinalAmount(SessionCart sessionCart) {
		int totalAmount = calculateSessionTotalAmount(sessionCart);
		int discountAmount = calculateSessionDiscountAmount(sessionCart);

		return totalAmount - discountAmount;
	}

	//ログイン後にセッションカートをDBカートへ取り込む
	// 1商品ずつ addCartItem に流すことで、在庫チェックや既存加算ロジックを共通化している
	@Transactional
	public void mergeSessionCartToUser(Integer userId, SessionCart sessionCart) {
		if (userId == null || sessionCart == null || sessionCart.isEmpty()) {
			return;
		}

		for (Map.Entry<Integer, Integer> entry : sessionCart.asUnmodifiableMap().entrySet()) {
			Integer productId = entry.getKey();
			Integer addQuantity = entry.getValue();

			//在庫チェック・既存商品への加算を addCartItem に統一
			addCartItem(userId, productId, addQuantity);
		}
	}
}