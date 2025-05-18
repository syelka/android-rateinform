// ExchangeRateApiService.kt
// Frissítsd ezt az interfészt
package com.syelka.rateinform

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApiService {
    @GET("latest")
    fun getLatestRates(
        @Query("from") fromCurrency: String,
        @Query("to") toCurrency: String
    ): Call<ExchangeRateResponse>

    // ÚJ: Végpont az elérhető valuták listájának lekéréséhez
    // Válasz példa: {"AUD":"Australian Dollar","BGN":"Bulgarian Lev", ...}
    @GET("currencies")
    fun getAvailableCurrencies(): Call<Map<String, String>> // A válasz egy Map, ahol a kulcs a valuta kódja, az érték a neve
}