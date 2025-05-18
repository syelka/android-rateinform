// FavoriteCurrencyPair.kt
// Hozz létre egy új Kotlin data class fájlt (pl. a fő csomagodban vagy egy 'data'/'model' alcsomagban)
package com.syelka.rateinform // Győződj meg róla, hogy ez a te csomagneved!

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Adat osztály a kedvenc valutapárok tárolására Firestore-ban
data class FavoriteCurrencyPair(
    val userId: String = "", // A felhasználó egyedi azonosítója (Firebase UID)
    val fromCurrency: String = "",
    val toCurrency: String = "",
    @ServerTimestamp // Automatikusan beállítja a szerver oldali időbélyeget létrehozáskor
    val createdAt: Date? = null
    // A dokumentum ID-t a Firestore automatikusan generálja, vagy mi is megadhatjuk.
    // Ha egyedi kombinációt akarunk (userId_from_to), akkor azt mi kezeljük.
)