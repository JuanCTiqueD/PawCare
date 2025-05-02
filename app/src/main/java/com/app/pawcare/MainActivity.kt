package com.app.pawcare

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configuración para modo oscuro o claro según la preferencia del sistema
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        auth = Firebase.auth
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Verificando información...")
        }

        val etEmail = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Correo y contraseña requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressDialog.show()
            autenticarUsuario(email, password)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun autenticarUsuario(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("LOGIN", "Inicio de sesión exitoso: ${user?.uid}")
                    verificarRolUsuario(user?.uid ?: "")
                } else {
                    progressDialog.dismiss()
                    Log.e("LOGIN", "Error al iniciar sesión", task.exception)
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun verificarRolUsuario(userId: String) {
        if (userId.isEmpty()) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error: ID de usuario no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnCompleteListener { task ->
                progressDialog.dismiss()

                if (task.isSuccessful) {
                    val rol = task.result?.getString("role") ?: "user"

                    val intent = when (rol) {
                        "caregiver" -> Intent(this, CuidadorActivity::class.java)
                        else -> Intent(this, ActivityInicio::class.java)
                    }

                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Error al verificar rol: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
