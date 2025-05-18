// RegisterActivity.kt
// Ez a fájl a java (vagy kotlin) -> com.example.yourpackagename könyvtárban található
package com.syelka.rateinform // Cseréld le a saját csomagnevedre!

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syelka.rateinform.R

class RegisterActivity : AppCompatActivity() {

    // Firebase Auth referencia
    private lateinit var auth: FirebaseAuth

    // UI Elemek referenciái
    private lateinit var editTextEmailRegister: EditText
    private lateinit var editTextPasswordRegister: EditText
    private lateinit var editTextConfirmPasswordRegister: EditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewLoginLink: TextView
    private lateinit var progressBarRegister: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // A layout fájlunk csatolása

        // Firebase Auth inicializálása
        auth = Firebase.auth

        // UI elemek inicializálása
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister)
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister)
        editTextConfirmPasswordRegister = findViewById(R.id.editTextConfirmPasswordRegister)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewLoginLink = findViewById(R.id.textViewLoginLink)
        progressBarRegister = findViewById(R.id.progressBarRegister)

        // Regisztráció gomb eseménykezelője
        buttonRegister.setOnClickListener {
            performRegistration()
        }

        // "Már van fiókod?" link eseménykezelője
        textViewLoginLink.setOnClickListener {
            // Navigálás a LoginActivity-re (ezt később hozzuk létre)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Bezárjuk a RegisterActivity-t, hogy ne lehessen visszanavigálni
        }
    }

    private fun performRegistration() {
        val email = editTextEmailRegister.text.toString().trim()
        val password = editTextPasswordRegister.text.toString().trim()
        val confirmPassword = editTextConfirmPasswordRegister.text.toString().trim()

        // Alapvető validációk
        if (email.isEmpty()) {
            editTextEmailRegister.error = "E-mail cím megadása kötelező!"
            editTextEmailRegister.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailRegister.error = "Érvénytelen e-mail formátum!"
            editTextEmailRegister.requestFocus()
            return
        }

        if (password.isEmpty()) {
            editTextPasswordRegister.error = "Jelszó megadása kötelező!"
            editTextPasswordRegister.requestFocus()
            return
        }

        if (password.length < 6) { // Firebase alapértelmezetten min. 6 karaktert vár
            editTextPasswordRegister.error = "A jelszónak legalább 6 karakter hosszúnak kell lennie!"
            editTextPasswordRegister.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            editTextConfirmPasswordRegister.error = "Jelszó megerősítése kötelező!"
            editTextConfirmPasswordRegister.requestFocus()
            return
        }

        if (password != confirmPassword) {
            editTextConfirmPasswordRegister.error = "A két jelszó nem egyezik!"
            editTextConfirmPasswordRegister.requestFocus()
            return
        }

        // Ha minden validáció sikeres, megjelenítjük a progress bart és elindítjuk a regisztrációt
        progressBarRegister.visibility = View.VISIBLE
        buttonRegister.isEnabled = false // Gomb letiltása a folyamat alatt

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBarRegister.visibility = View.GONE // Progress bar elrejtése
                buttonRegister.isEnabled = true // Gomb újra engedélyezése

                if (task.isSuccessful) {
                    // Sikeres regisztráció
                    Toast.makeText(baseContext, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show()

                    // Opcionálisan: E-mail verifikáció küldése
                    // val user = auth.currentUser
                    // user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                    //     if (verificationTask.isSuccessful) {
                    //         Toast.makeText(baseContext, "Megerősítő e-mail elküldve.", Toast.LENGTH_LONG).show()
                    //     }
                    // }

                    // Navigálás a főképernyőre vagy a bejelentkezési képernyőre
                    // Itt most a LoginActivity-re navigálunk, hogy a felhasználó bejelentkezhessen
                    val intent = Intent(this, LoginActivity::class.java)
                    // Tisztítjuk a back stack-et, hogy a felhasználó ne tudjon visszanavigálni a regisztrációs képernyőre
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {
                    // Sikertelen regisztráció
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        // Ha az e-mail cím már foglalt
                        editTextEmailRegister.error = "Ez az e-mail cím már regisztrálva van."
                        editTextEmailRegister.requestFocus()
                    } catch (e: Exception) {
                        Toast.makeText(baseContext, "Sikertelen regisztráció: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    // Javasolt felülírni onStart-ot, hogy ellenőrizzük, be van-e már lépve a felhasználó
    // Ha igen, akkor átirányíthatjuk a főképernyőre.
    // Ezt a logikát a LoginActivity-ben és egy "Splash" vagy "Launcher" Activity-ben is érdemes megvalósítani.
    /*
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Felhasználó már be van jelentkezve, átirányítás a fő Activity-re
            // val intent = Intent(this, MainActivity::class.java)
            // startActivity(intent)
            // finish()
        }
    }
    */
}