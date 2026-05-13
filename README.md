# Coinflow Card Form SDK - Android

A Jetpack Compose SDK for integrating Coinflow's card tokenization forms into Android apps.

## Requirements

- Android `minSdk` 24+
- Kotlin 1.9+
- Jetpack Compose

## Installation

Add Maven Central to your repositories (if not already present) and add the dependency:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// app/build.gradle.kts
dependencies {
    implementation("cash.coinflow:coinflow-card-form:0.1.0")
}
```

## Usage

```kotlin
import cash.coinflow.cardform.*

@Composable
fun PaymentScreen() {
    val controller = remember { CoinflowCardFormController() }
    val scope = rememberCoroutineScope()

    Column {
        CoinflowCardFormView(
            variant = CardFormVariant.CARD_FORM,
            merchantId = "your-merchant-id",
            env = CoinflowEnv.SANDBOX,
            controller = controller,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )

        Button(onClick = {
            scope.launch {
                try {
                    val response = controller.tokenize()
                    println("Token: ${response.token}")
                } catch (e: Exception) {
                    // surface error to user
                }
            }
        }) {
            Text("Tokenize")
        }
    }
}
```

`controller.tokenize()` is a `suspend` function. Call it from a coroutine scope. It returns a `TokenizeResponse` with:

- `token: String` — payment token to send to your backend
- `expMonth: String?`, `expYear: String?` — only populated for variants that collect expiry

## Configuration

### Merchant ID

Issued by Coinflow when you sign up — find it in the merchant dashboard. The same value works for both sandbox and production. Typically read from a `BuildConfig` field or env var:

```kotlin
val merchantId = BuildConfig.COINFLOW_MERCHANT_ID
```

### Environment

Selects which Coinflow backend the form talks to:

- `CoinflowEnv.SANDBOX` — test cards, no real money. Use during integration and QA.
- `CoinflowEnv.PROD` — live cards, real money.

The other values (`STAGING`, `LOCAL`) are Coinflow-internal and not intended for integrators.

Typical pattern:

```kotlin
val env = if (BuildConfig.DEBUG) CoinflowEnv.SANDBOX else CoinflowEnv.PROD
```

## Variants

- `CardFormVariant.CARD_FORM` — full card entry (number, expiry, CVV)
- `CardFormVariant.CARD_NUMBER_FORM` — card number + expiry only; returns a token used to render `CVV_FORM` later
- `CardFormVariant.CVV_FORM` — CVV only; requires a saved card `token` parameter

## Theming

`MerchantTheme` styles the rendered form to match your app. All fields are optional — pass only what you want to override. Minimal example:

```kotlin
val theme = MerchantTheme(primary = "#FF6600", style = MerchantStyle.ROUNDED)
```

Full example:

```kotlin
val theme = MerchantTheme(
    primary = "#165DFB",
    background = "#ffffff",
    backgroundAccent = "#F3F4F6",
    backgroundAccent2 = "#E4E7EB",
    textColor = "#05092E",
    textColorAccent = "#030712",
    textColorAction = "#ffffff",
    ctaColor = "#165DFB",
    font = "Red Hat Display",
    style = MerchantStyle.ROUNDED
)
```

Pass into `CoinflowCardFormView(theme = theme, ...)`.

### Theme fields

| Field | Purpose |
| --- | --- |
| `primary`, `ctaColor` | Accent / action colors (hex strings) |
| `background`, `backgroundAccent`, `backgroundAccent2` | Form background tones |
| `textColor`, `textColorAccent`, `textColorAction` | Input and label text colors |
| `font`, `fontSize`, `fontWeight` | Typography. `font` must be available on the device. |
| `style` | Input shape: `ROUNDED`, `SHARP`, `PILL` |
| `cardNumberPlaceholder`, `cvvPlaceholder`, `expirationPlaceholder` | Override input placeholder text |
| `showCardIcon` | Toggle the card brand icon (Visa/Mastercard/Amex) |

## License

Apache 2.0 — see [LICENSE](./LICENSE).
