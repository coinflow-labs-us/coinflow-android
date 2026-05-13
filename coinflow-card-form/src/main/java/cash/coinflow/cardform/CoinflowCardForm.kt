package cash.coinflow.cardform

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoinflowCardFormController {
    internal var webView: WebView? = null
    internal var tokenizeContinuation: Continuation<CardFormTokenResponse>? = null
    var isLoaded by mutableStateOf(false)
        internal set

    suspend fun tokenize(): CardFormTokenResponse {
        val wv = webView ?: throw CoinflowException("Card form WebView not loaded")
        if (!isLoaded) throw CoinflowException("Card form not yet loaded")
        if (tokenizeContinuation != null) throw CoinflowException("Tokenize already in progress")

        return suspendCancellableCoroutine { continuation ->
            tokenizeContinuation = continuation
            continuation.invokeOnCancellation { tokenizeContinuation = null }
            wv.post {
                wv.evaluateJavascript("window.postMessage('tokenize', '*')", null)
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CoinflowCardFormView(
    variant: CardFormVariant = CardFormVariant.CARD_FORM,
    merchantId: String,
    env: CoinflowEnv? = null,
    theme: MerchantTheme? = null,
    token: String? = null,
    controller: CoinflowCardFormController,
    modifier: Modifier = Modifier,
    onLoad: (() -> Unit)? = null
) {
    val url = remember(variant, merchantId, env, theme, token) {
        buildUrl(variant, merchantId, env, theme, token)
    }
    val loadedUrl = remember { mutableStateOf<String?>(null) }

    DisposableEffect(controller) {
        onDispose {
            controller.webView = null
            controller.isLoaded = false
            controller.tokenizeContinuation?.let {
                controller.tokenizeContinuation = null
                it.resumeWithException(CoinflowException("Card form disposed"))
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                addJavascriptInterface(
                    CoinflowJsBridge(controller, onLoad),
                    "CoinflowAndroid"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            """
                            window.addEventListener('message', function(e) {
                                if (e.origin !== window.location.origin) return;
                                CoinflowAndroid.onMessage(typeof e.data === 'string' ? e.data : JSON.stringify(e.data));
                            });
                            """.trimIndent(),
                            null
                        )
                    }
                }

                controller.webView = this
                loadedUrl.value = url
                loadUrl(url)
            }
        },
        update = { webView ->
            if (loadedUrl.value != url) {
                loadedUrl.value = url
                controller.isLoaded = false
                webView.loadUrl(url)
            }
        }
    )
}

internal fun buildUrl(
    variant: CardFormVariant,
    merchantId: String,
    env: CoinflowEnv?,
    theme: MerchantTheme?,
    token: String?
): String {
    val baseUrl = CoinflowUtils.getBaseUrl(env)
    val builder = Uri.parse("$baseUrl/form/v2/${variant.value}").buildUpon()
        .appendQueryParameter("merchantId", merchantId)
        .appendQueryParameter("source", "android-sdk")

    theme?.let {
        builder.appendQueryParameter(
            "theme",
            LZString.compressToEncodedURIComponent(it.toJson())
        )
    }

    token?.let {
        builder.appendQueryParameter("token", it)
    }

    return builder.build().toString()
}

private class CoinflowJsBridge(
    private val controller: CoinflowCardFormController,
    private val onLoad: (() -> Unit)?
) {
    @JavascriptInterface
    fun onMessage(message: String) {
        val parsed = try {
            JSONObject(message)
        } catch (_: Exception) {
            return
        }
        val method = parsed.optString("method")

        if (method == "loaded") {
            controller.isLoaded = true
            onLoad?.invoke()
        }

        if (method == "tokenize") {
            handleTokenizeResponse(parsed)
        }
    }

    private fun handleTokenizeResponse(parsed: JSONObject) {
        val continuation = controller.tokenizeContinuation ?: return
        controller.tokenizeContinuation = null
        parseTokenizeResponse(parsed)
            .onSuccess { continuation.resume(it) }
            .onFailure { continuation.resumeWithException(it) }
    }
}

internal fun parseTokenizeResponse(parsed: JSONObject): Result<CardFormTokenResponse> {
    val data = parsed.opt("data")

    if (data is String) {
        if (data.startsWith("ERROR ")) {
            return Result.failure(CoinflowException(data.removePrefix("ERROR ")))
        }
        return try {
            Result.success(JSONObject(data).toTokenResponse())
        } catch (_: Exception) {
            Result.failure(CoinflowException("Invalid response"))
        }
    }

    if (data is JSONObject) {
        return Result.success(data.toTokenResponse())
    }

    return Result.failure(CoinflowException("Invalid response"))
}

private fun JSONObject.toTokenResponse() = CardFormTokenResponse(
    token = optString("token", ""),
    expMonth = if (has("expMonth") && !isNull("expMonth")) getString("expMonth") else null,
    expYear = if (has("expYear") && !isNull("expYear")) getString("expYear") else null
)

class CoinflowException(message: String) : Exception(message)
