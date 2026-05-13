package cash.coinflow.cardform

import org.junit.Assert.assertEquals
import org.junit.Test

class CoinflowUtilsTest {
    @Test
    fun nullEnvDefaultsToProd() {
        assertEquals("https://coinflow.cash", CoinflowUtils.getBaseUrl(null))
    }

    @Test
    fun prodEnv() {
        assertEquals("https://coinflow.cash", CoinflowUtils.getBaseUrl(CoinflowEnv.PROD))
    }

    @Test
    fun sandboxEnv() {
        assertEquals("https://sandbox.coinflow.cash", CoinflowUtils.getBaseUrl(CoinflowEnv.SANDBOX))
    }

    @Test
    fun stagingEnvUsesHyphenatedHost() {
        assertEquals("https://staging.coinflow.cash", CoinflowUtils.getBaseUrl(CoinflowEnv.STAGING))
    }

    @Test
    fun localEnvUsesLocalhost() {
        assertEquals("http://localhost:3000", CoinflowUtils.getBaseUrl(CoinflowEnv.LOCAL))
    }
}
