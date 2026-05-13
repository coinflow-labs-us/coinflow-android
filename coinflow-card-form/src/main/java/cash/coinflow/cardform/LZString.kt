package cash.coinflow.cardform

internal object LZString {
    private const val KEY_STR_URI_SAFE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$"

    fun compressToEncodedURIComponent(input: String): String {
        if (input.isEmpty()) return ""
        val compressed = compress(input)
        return baseEncode(compressed, KEY_STR_URI_SAFE)
    }

    private fun compress(uncompressed: String): List<Int> {
        val dictionary = mutableMapOf<String, Int>()
        val dictionaryToCreate = mutableSetOf<String>()
        var dictSize = 3
        var numBits = 2
        var enlargeIn = 2
        var w = ""
        val result = mutableListOf<Int>()

        fun emitLiteral(s: String) {
            val charCode = s[0].code
            if (charCode < 256) {
                repeat(numBits) { result.add(0) }
                result.addAll(bits(charCode, 8))
            } else {
                result.addAll(bits(1, numBits))
                result.addAll(bits(charCode, 16))
            }
            enlargeIn--
            if (enlargeIn == 0) {
                enlargeIn = 1 shl numBits
                numBits++
            }
            dictionaryToCreate.remove(s)
        }

        fun emitW() {
            if (w in dictionaryToCreate) {
                emitLiteral(w)
            } else {
                result.addAll(bits(dictionary[w]!!, numBits))
            }
            enlargeIn--
            if (enlargeIn == 0) {
                enlargeIn = 1 shl numBits
                numBits++
            }
        }

        for (c in uncompressed) {
            val sc = c.toString()
            if (!dictionary.containsKey(sc)) {
                dictionary[sc] = dictSize++
                dictionaryToCreate.add(sc)
            }
            val wc = w + sc
            if (dictionary.containsKey(wc)) {
                w = wc
            } else {
                emitW()
                dictionary[wc] = dictSize++
                w = sc
            }
        }

        if (w.isNotEmpty()) {
            emitW()
        }

        result.addAll(bits(2, numBits))

        return result
    }

    private fun bits(value: Int, count: Int): List<Int> {
        return (0 until count).map { (value shr it) and 1 }
    }

    private fun baseEncode(data: List<Int>, alphabet: String): String {
        val sb = StringBuilder()
        var value = 0
        var position = 0

        for (bit in data) {
            value = (value shl 1) or bit
            position++
            if (position == 6) {
                sb.append(alphabet[value])
                value = 0
                position = 0
            }
        }

        if (position > 0) {
            value = value shl (6 - position)
            sb.append(alphabet[value])
        }

        return sb.toString()
    }
}
