// LoginActivity.kt
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var editTextEmailLogin: EditText
    private lateinit var editTextPasswordLogin: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegisterLink: TextView
    private lateinit var progressBarLogin: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        editTextEmailLogin = findViewById(R.id.editTextEmailLogin)
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink)
        progressBarLogin = findViewById(R.id.progressBarLogin)

        buttonLogin.setOnClickListener {
            performLogin()
        }

        textViewRegisterLink.setOnClickListener {
            // Navigálás a RegisterActivity-re
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            // Nem hívunk finish()-t, hogy vissza lehessen navigálni a bejelentkezéshez, ha meggondolja magát
        }
    }

    // Ellenőrizzük, hogy a felhasználó be van-e már jelentkezve az Activity indításakor
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Felhasználó már be van jelentkezve, átirányítás a fő Activity-re
            // Cseréld le a MainActivity::class.java-t a te fő Activity-d nevére!
            Toast.makeText(baseContext, "Már be vagy jelentkezve!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java) // Tegyük fel, hogy van egy MainActivity-d
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun performLogin() {
        val email = editTextEmailLogin.text.toString().trim()
        val password = editTextPasswordLogin.text.toString().trim()

        if (email.isEmpty()) {
            editTextEmailLogin.error = "E-mail cím megadása kötelező!"
            editTextEmailLogin.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailLogin.error = "Érvénytelen e-mail formátum!"
            editTextEmailLogin.requestFocus()
            return
        }

        if (password.isEmpty()) {
            editTextPasswordLogin.error = "Jelszó megadása kötelező!"
            editTextPasswordLogin.requestFocus()
            return
        }

        // Firebase jelszó hossza itt nem releváns a bejelentkezésnél, csak a regisztrációnál.

        progressBarLogin.visibility = View.VISIBLE
        buttonLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBarLogin.visibility = View.GONE
                buttonLogin.isEnabled = true

                if (task.isSuccessful) {
                    // Sikeres bejelentkezés
                    Toast.makeText(baseContext, "Sikeres bejelentkezés!", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    // Navigálás a főképernyőre
                    // Cseréld le a MainActivity::class.java-t a te fő Activity-d nevére!
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Kitörli a back stack-et
                    startActivity(intent)
                    finish() // Bezárja a LoginActivity-t
                } else {
                    // Sikertelen bejelentkezés
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        editTextEmailLogin.error = "Nincs ilyen e-mail címmel regisztrált felhasználó."
                        editTextEmailLogin.requestFocus()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        editTextPasswordLogin.error = "Helytelen jelszó."
                        editTextPasswordLogin.requestFocus()
                    }
                    catch (e: Exception) {
                        Toast.makeText(baseContext, "Sikertelen bejelentkezés: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}