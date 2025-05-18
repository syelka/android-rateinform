// MainActivity.kt
// Jelentősen frissítve a Firestore integrációval
package com.syelka.rateinform

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // ÚJ: Firestore referencia

    private lateinit var textViewUserEmail: TextView
    private lateinit var buttonLogout: Button
    private lateinit var textViewExchangeRateValue: TextView
    private lateinit var spinnerFromCurrency: Spinner
    private lateinit var spinnerToCurrency: Spinner
    private lateinit var buttonGetExchangeRate: Button
    private lateinit var buttonSaveFavorite: Button // ÚJ

    private lateinit var exchangeRateApiService: ExchangeRateApiService
    private var availableCurrencies: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        db = Firebase.firestore // ÚJ: Firestore inicializálása

        textViewUserEmail = findViewById(R.id.textViewUserEmail)
        buttonLogout = findViewById(R.id.buttonLogout)
        textViewExchangeRateValue = findViewById(R.id.textViewExchangeRateValue)
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency)
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency)
        buttonGetExchangeRate = findViewById(R.id.buttonGetExchangeRate)
        buttonSaveFavorite = findViewById(R.id.buttonSaveFavorite) // ÚJ

        val currentUser = auth.currentUser
        if (currentUser != null) {
            textViewUserEmail.text = currentUser.email ?: "Nincs e-mail cím"
        } else {
            navigateToLogin()
            return
        }

        buttonLogout.setOnClickListener {
            performLogout()
        }

        // HELYES RÉSZ
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/") // EZ A HELYES FORMÁTUM
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        exchangeRateApiService = retrofit.create(ExchangeRateApiService::class.java)

        fetchAvailableCurrencies()

        buttonGetExchangeRate.setOnClickListener {
            handleGetExchangeRate()
        }

        // ÚJ: Kedvenc mentése gomb eseménykezelője
        buttonSaveFavorite.setOnClickListener {
            saveFavoriteCurrencyPair()
        }
    }

    private fun handleGetExchangeRate() {
        val fromCurrency = spinnerFromCurrency.selectedItem?.toString()
        val toCurrency = spinnerToCurrency.selectedItem?.toString()

        if (fromCurrency.isNullOrEmpty() || toCurrency.isNullOrEmpty()) {
            textViewExchangeRateValue.text = "-"
            Toast.makeText(this, "Kérlek válassz forrás és cél valutát.", Toast.LENGTH_SHORT).show()
            return
        }

        if (fromCurrency == toCurrency) {
            textViewExchangeRateValue.text = "A forrás és cél valuta nem lehet azonos."
            Toast.makeText(this, "A forrás és cél valuta nem lehet azonos.", Toast.LENGTH_SHORT).show()
            return
        }
        fetchExchangeRate(fromCurrency, toCurrency)
    }

    private fun saveFavoriteCurrencyPair() {
        val userId = auth.currentUser?.uid
        val fromCurrency = spinnerFromCurrency.selectedItem?.toString()
        val toCurrency = spinnerToCurrency.selectedItem?.toString()

        if (userId == null) {
            Toast.makeText(this, "Hiba: Felhasználó nincs bejelentkezve.", Toast.LENGTH_SHORT).show()
            return
        }

        if (fromCurrency.isNullOrEmpty() || toCurrency.isNullOrEmpty()) {
            Toast.makeText(this, "Kérlek válassz valutapárt a mentéshez.", Toast.LENGTH_SHORT).show()
            return
        }

        if (fromCurrency == toCurrency) {
            Toast.makeText(this, "Azonos valutákat nem lehet kedvencként menteni.", Toast.LENGTH_SHORT).show()
            return
        }

        val favoritePair = FavoriteCurrencyPair(
            userId = userId,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency
            // createdAt automatikusan generálódik a @ServerTimestamp miatt
        )

        // Egyedi dokumentum ID létrehozása a duplikációk elkerülése érdekében (opcionális, de ajánlott)
        // Így egy felhasználó csak egyszer menthet el egy adott valutapárt.
        val documentId = "${userId}_${fromCurrency}_${toCurrency}"

        db.collection("favoritePairs").document(documentId)
            .set(favoritePair)
            .addOnSuccessListener {
                Log.d("MainActivity", "Kedvenc sikeresen mentve: $documentId")
                Toast.makeText(this, "Kedvencként mentve: $fromCurrency/$toCurrency", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("MainActivity", "Hiba a kedvenc mentésekor", e)
                Toast.makeText(this, "Hiba a kedvenc mentésekor: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun fetchAvailableCurrencies() {
        exchangeRateApiService.getAvailableCurrencies().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    response.body()?.let { currenciesMap ->
                        availableCurrencies = currenciesMap.keys.toList().sorted()
                        setupSpinners()
                    }
                } else {
                    Log.e("MainActivity", "Hiba az elérhető valuták lekérésekor: ${response.code()}")
                    Toast.makeText(this@MainActivity, "Hiba a valuták betöltésekor.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("MainActivity", "Hálózati hiba az elérhető valuták lekérésekor", t)
                Toast.makeText(this@MainActivity, "Hálózati hiba a valuták betöltésekor.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSpinners() {
        if (availableCurrencies.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableCurrencies)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerFromCurrency.adapter = adapter
            spinnerToCurrency.adapter = adapter

            val defaultFrom = "EUR"
            val defaultTo = "HUF"
            if (availableCurrencies.contains(defaultFrom)) {
                spinnerFromCurrency.setSelection(availableCurrencies.indexOf(defaultFrom))
            }
            if (availableCurrencies.contains(defaultTo)) {
                spinnerToCurrency.setSelection(availableCurrencies.indexOf(defaultTo))
            }
        }
    }

    private fun fetchExchangeRate(from: String, to: String) {
        textViewExchangeRateValue.text = "Árfolyam lekérése..."
        exchangeRateApiService.getLatestRates(from, to).enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful) {
                    val exchangeRateData = response.body()
                    if (exchangeRateData?.rates != null && exchangeRateData.rates.containsKey(to)) {
                        val rate = exchangeRateData.rates[to]
                        textViewExchangeRateValue.text = "1 $from = $rate $to"
                    } else {
                        textViewExchangeRateValue.text = "Hiba: Adat nem elérhető."
                        Log.e("MainActivity", "API válasz nem tartalmazza a várt adatokat: ${response.body().toString()}")
                    }
                } else {
                    textViewExchangeRateValue.text = "Hiba: ${response.code()}"
                    Log.e("MainActivity", "API hiba kód: ${response.code()}, üzenet: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                textViewExchangeRateValue.text = "Hálózati hiba."
                Log.e("MainActivity", "API hívás sikertelen", t)
            }
        })
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(baseContext, "Sikeres kijelentkezés!", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}