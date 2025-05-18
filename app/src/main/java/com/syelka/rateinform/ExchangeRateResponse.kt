// ExchangeRateResponse.kt
// Hozz létre egy új Kotlin fájlt (pl. a fő csomagodban vagy egy 'network' alcsomagban)
package com.syelka.rateinform // Győződj meg róla, hogy ez a te csomagneved!

import com.google.gson.annotations.SerializedName

// Adatosztály a Frankfurter API válaszának feldolgozásához
// Példa válasz: {"amount":1.0,"base":"EUR","date":"2024-05-17","rates":{"HUF":387.08}}
data class ExchangeRateResponse(
    @SerializedName("amount")
    val amount: Double?,
    @SerializedName("base")
    val base: String?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("rates")
    val rates: Map<String, Double>? // A "rates" egy objektum, ahol a kulcs a célvaluta (pl. "HUF")
)