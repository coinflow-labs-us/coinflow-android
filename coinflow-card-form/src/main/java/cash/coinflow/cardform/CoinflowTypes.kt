package cash.coinflow.cardform

import org.json.JSONObject

enum class CoinflowEnv(val value: String) {
    PROD("prod"),
    STAGING("staging"),
    SANDBOX("sandbox"),
    LOCAL("local")
}

enum class CardFormVariant(val value: String) {
    CARD_FORM("card-form"),
    CARD_NUMBER_FORM("card-number-form"),
    CVV_FORM("cvv-form")
}

enum class MerchantStyle(val value: String) {
    ROUNDED("rounded"),
    SHARP("sharp"),
    PILL("pill")
}

data class MerchantTheme(
    val primary: String? = null,
    val background: String? = null,
    val backgroundAccent: String? = null,
    val backgroundAccent2: String? = null,
    val textColor: String? = null,
    val textColorAccent: String? = null,
    val textColorAction: String? = null,
    val ctaColor: String? = null,
    val font: String? = null,
    val style: MerchantStyle? = null,
    val fontSize: String? = null,
    val fontWeight: String? = null,
    val cardNumberPlaceholder: String? = null,
    val cvvPlaceholder: String? = null,
    val expirationPlaceholder: String? = null,
    val showCardIcon: Boolean? = null
) {
    fun toJson(): String {
        val json = JSONObject()
        primary?.let { json.put("primary", it) }
        background?.let { json.put("background", it) }
        backgroundAccent?.let { json.put("backgroundAccent", it) }
        backgroundAccent2?.let { json.put("backgroundAccent2", it) }
        textColor?.let { json.put("textColor", it) }
        textColorAccent?.let { json.put("textColorAccent", it) }
        textColorAction?.let { json.put("textColorAction", it) }
        ctaColor?.let { json.put("ctaColor", it) }
        font?.let { json.put("font", it) }
        style?.let { json.put("style", it.value) }
        fontSize?.let { json.put("fontSize", it) }
        fontWeight?.let { json.put("fontWeight", it) }
        cardNumberPlaceholder?.let { json.put("cardNumberPlaceholder", it) }
        cvvPlaceholder?.let { json.put("cvvPlaceholder", it) }
        expirationPlaceholder?.let { json.put("expirationPlaceholder", it) }
        showCardIcon?.let { json.put("showCardIcon", it) }
        return json.toString()
    }
}

data class CardFormTokenResponse(
    val token: String,
    val expMonth: String? = null,
    val expYear: String? = null
)
