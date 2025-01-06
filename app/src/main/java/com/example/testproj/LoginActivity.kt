package com.example.testproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerTextView: TextView = findViewById(R.id.textViewRegister)

        // Când utilizatorul apasă butonul de login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email și parolă sunt necesare", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        // Când utilizatorul apasă pe "Înregistrează-te"
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Autentificare reușită
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()  // Închide LoginActivity după succes
                } else {
                    // Eroare la autentificare
                    val errorMessage = task.exception?.message ?: "Necunoscută"
                    Toast.makeText(this, "Autentificare eșuată: $errorMessage", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}