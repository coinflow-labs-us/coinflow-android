package cash.coinflow.cardform

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TokenizeResponseParserTest {
    private fun message(data: Any): JSONObject =
        JSONObject().put("method", "tokenize").put("data", data)

    @Test
    fun parsesStringifiedJsonWithTokenOnly() {
        val msg = message("""{"token":"tok_abc123"}""")
        val response = parseTokenizeResponse(msg).getOrThrow()
        assertEquals("tok_abc123", response.token)
        assertNull(response.expMonth)
        assertNull(response.expYear)
    }

    @Test
    fun parsesStringifiedJsonWithExpiry() {
        val msg = message("""{"token":"tok_xyz","expMonth":"03","expYear":"2030"}""")
        val response = parseTokenizeResponse(msg).getOrThrow()
        assertEquals("tok_xyz", response.token)
        assertEquals("03", response.expMonth)
        assertEquals("2030", response.expYear)
    }

    @Test
    fun parsesNestedObjectData() {
        val msg = message(JSONObject().put("token", "tok_obj").put("expMonth", "11").put("expYear", "2029"))
        val response = parseTokenizeResponse(msg).getOrThrow()
        assertEquals("tok_obj", response.token)
        assertEquals("11", response.expMonth)
    }

    @Test
    fun treatsNullExpiryAsAbsent() {
        val msg = message("""{"token":"tok_abc","expMonth":null,"expYear":null}""")
        val response = parseTokenizeResponse(msg).getOrThrow()
        assertNull(response.expMonth)
        assertNull(response.expYear)
    }

    @Test
    fun errorPrefixedStringBecomesCoinflowException() {
        val msg = message("ERROR Card declined")
        val ex = parseTokenizeResponse(msg).exceptionOrNull()
        assertTrue(ex is CoinflowException)
        assertEquals("Card declined", ex!!.message)
    }

    @Test
    fun malformedJsonStringBecomesInvalidResponse() {
        val msg = message("{not-json")
        val ex = parseTokenizeResponse(msg).exceptionOrNull()
        assertTrue(ex is CoinflowException)
        assertEquals("Invalid response", ex!!.message)
    }

    @Test
    fun missingDataBecomesInvalidResponse() {
        val msg = JSONObject().put("method", "tokenize")
        val ex = parseTokenizeResponse(msg).exceptionOrNull()
        assertTrue(ex is CoinflowException)
        assertEquals("Invalid response", ex!!.message)
    }

    @Test
    fun missingTokenFieldYieldsEmptyToken() {
        val msg = message("""{"expMonth":"01"}""")
        val response = parseTokenizeResponse(msg).getOrThrow()
        assertEquals("", response.token)
        assertEquals("01", response.expMonth)
    }
}
