// ECフォーム共通処理クラス
class EcFormUtils {

	/**
	 * 数値のみ許可し、最大桁数を制限
	 * @param selector 対象input
	 * @param maxLength 最大桁数
	 */
	static normalizeNumeric(selector, maxLength) {
		const inputs = document.querySelectorAll(selector);

		inputs.forEach(input => {
			input.addEventListener('input', () => {

				let normalizedValue = input.value.replace(/[^0-9]/g, '');

				if (normalizedValue.length > maxLength) {
					normalizedValue = normalizedValue.slice(0, maxLength);
				}

				input.value = normalizedValue;
			});
		});
	}
}

// DOM読込後に初期化
document.addEventListener('DOMContentLoaded', () => {

	// 電話番号（11桁）
	EcFormUtils.normalizeNumeric('input[name="phoneNumber"]', 11);

	// 郵便番号（7桁）
	EcFormUtils.normalizeNumeric('input[name="postalCode"]', 7);

});

// 修正（JS）：お気に入りトグル（CSRF付き）
document.addEventListener("click", async (event) => {
	const button = event.target.closest(".favorite-button");
	if (!button) return;

	event.preventDefault();
	event.stopPropagation();

	const productId = button.dataset.productId;
	const icon = button.querySelector(".bi");
	if (!productId || !icon) return;

	const isFilled = icon.classList.contains("bi-heart-fill");

	const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

	const url = isFilled ? `/favorites/${productId}/delete` : `/favorites/${productId}`;

	const headers = {};
	if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

	const response = await fetch(url, { method: "POST", headers });

	if (!response.ok) {
		console.warn("favorite failed", response.status);
		return;
	}

	icon.classList.toggle("bi-heart-fill", !isFilled);
	icon.classList.toggle("bi-heart", isFilled);
}, true);

// 修正（JS）：検索バーを開閉する
document.addEventListener("DOMContentLoaded", function() {

	const searchToggleButton = document.getElementById("searchToggleButton");
	const searchOverlay = document.getElementById("searchOverlay");
	const searchKeyword = document.getElementById("searchKeyword");

	if (!searchToggleButton || !searchOverlay) {
		return;
	}

	searchToggleButton.addEventListener("click", function(event) {
		event.stopPropagation();

		searchOverlay.classList.toggle("is-open");

		if (searchOverlay.classList.contains("is-open") && searchKeyword) {
			setTimeout(function() {
				searchKeyword.focus();
			}, 150);
		}
	});

	searchOverlay.addEventListener("click", function(event) {
		event.stopPropagation();
	});

	document.addEventListener("click", function() {
		searchOverlay.classList.remove("is-open");
	});

	document.addEventListener("keydown", function(event) {
		if (event.key === "Escape") {
			searchOverlay.classList.remove("is-open");
		}
	});

});