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