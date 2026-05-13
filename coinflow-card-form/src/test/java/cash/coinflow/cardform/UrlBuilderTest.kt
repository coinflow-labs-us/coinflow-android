package cash.coinflow.cardform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlBuilderTest {
    @Test
    fun defaultEnvNoThemeOrToken() {
        val url = buildUrl(
            variant = CardFormVariant.CARD_FORM,
            merchantId = "merchant_abc",
            env = null,
            theme = null,
            token = null
        )
        assertEquals(
            "https://coinflow.cash/form/v2/card-form?merchantId=merchant_abc&source=android-sdk",
            url
        )
    }

    @Test
    fun cvvVariantOnSandbox() {
        val url = buildUrl(
            variant = CardFormVariant.CVV_FORM,
            merchantId = "m1",
            env = CoinflowEnv.SANDBOX,
            theme = null,
            token = null
        )
        assertEquals(
            "https://sandbox.coinflow.cash/form/v2/cvv-form?merchantId=m1&source=android-sdk",
            url
        )
    }

    @Test
    fun includesTokenParam() {
        val url = buildUrl(
            variant = CardFormVariant.CARD_NUMBER_FORM,
            merchantId = "m1",
            env = null,
            theme = null,
            token = "tok_123"
        )
        assertTrue(url.contains("token=tok_123"))
        assertTrue(url.contains("merchantId=m1"))
        assertTrue(url.contains("source=android-sdk"))
        assertTrue(url.contains("/form/v2/card-number-form"))
    }

    @Test
    fun compressesThemeIntoQuery() {
        val theme = MerchantTheme(primary = "#ff0000", style = MerchantStyle.PILL)
        val url = buildUrl(
            variant = CardFormVariant.CARD_FORM,
            merchantId = "m1",
            env = null,
            theme = theme,
            token = null
        )
        assertTrue(url.contains("theme="))
        assertFalse(url.contains("primary"))
        assertFalse(url.contains("#ff0000"))
    }
}
