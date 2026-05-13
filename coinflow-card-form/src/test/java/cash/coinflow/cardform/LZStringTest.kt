package cash.coinflow.cardform

import org.junit.Assert.assertEquals
import org.junit.Test

class LZStringTest {
    @Test
    fun emptyStringReturnsEmpty() {
        assertEquals("", LZString.compressToEncodedURIComponent(""))
    }

    // Golden fixtures captured from pieroxy/lz-string (npm: lz-string) — the canonical JS
    // implementation that the Coinflow form runs. Our Kotlin port MUST byte-for-byte match.
    @Test fun matchesJsRefHelloWorld() =
        assertEquals("BYUwNmD2AEDukCcwBMg", LZString.compressToEncodedURIComponent("hello world"))

    @Test fun matchesJsRefSingleChar() =
        assertEquals("IZA", LZString.compressToEncodedURIComponent("a"))

    @Test fun matchesJsRefRepeatingPattern() =
        assertEquals("IIIV7Sg", LZString.compressToEncodedURIComponent("ABABABABABAB"))

    @Test fun matchesJsRefThemeJson() =
        assertEquals(
            "N4IgDgTglgtghhAniAXCAxARgGwFYAiAYgEIgA0IAzgC6IA2ApqiBAPYCuAdgCYPcgBfIA",
            LZString.compressToEncodedURIComponent("""{"primary":"#165DFB","style":"rounded"}""")
        )

    @Test fun matchesJsRefTokenResponse() =
        assertEquals(
            "N4IgLg9g1gpgdiAXOaB9AhgIwMYEYBMAzCAL5A",
            LZString.compressToEncodedURIComponent("""{"token":"tok_abc123"}""")
        )
}
