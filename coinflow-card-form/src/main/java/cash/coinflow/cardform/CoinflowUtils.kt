package cash.coinflow.cardform

internal object CoinflowUtils {
    fun getBaseUrl(env: CoinflowEnv?): String {
        return when (env) {
            null, CoinflowEnv.PROD -> "https://coinflow.cash"
            CoinflowEnv.LOCAL -> "http://localhost:3000"
            else -> "https://${env.value}.coinflow.cash"
        }
    }
}
