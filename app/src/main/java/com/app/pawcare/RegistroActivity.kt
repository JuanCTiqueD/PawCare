package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = Firebase.auth

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val switchUser = findViewById<Switch>(R.id.switchUser)
        val switchCaregiver = findViewById<Switch>(R.id.switchCaregiver)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val isUser = switchUser.isChecked
            val isCaregiver = switchCaregiver.isChecked

            if (username.isEmpty()) {
                etUsername.error = "Nombre de usuario requerido"
                etUsername.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Correo electrónico requerido"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (!isUser && !isCaregiver) {
                Toast.makeText(this, "Debes seleccionar un rol", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("FIREBASE", "Iniciando creación de usuario con email: $email")

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val role = if (isUser) "user" else "caregiver"
                        val userData = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "role" to role
                        )

                        Log.d("FIREBASE", "Usuario creado en Auth con UID: ${user?.uid}")
                        Log.d("FIREBASE", "Guardando usuario en Firestore...")

                        user?.let {
                            db.collection("users")
                                .document(it.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Log.d("FIREBASE", "Usuario guardado con éxito en Firestore")
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                                    // Aquí redireccionamos según el rol
                                    if (isCaregiver) {
                                        val intent = Intent(this, CuidadorActivity::class.java)
                                        startActivity(intent)
                                    } else if (isUser) {
                                        val intent = Intent(this, ActivityInicio::class.java)
                                        startActivity(intent)
                                    }
                                    finish() // Cerramos esta actividad para que no vuelva atrás
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FIREBASE", "Error al guardar en Firestore", e)
                                    Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.e("FIREBASE", "Error en el registro: ${task.exception?.message}", task.exception)
                        Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        switchUser.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchCaregiver.isChecked = false
        }

        switchCaregiver.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchUser.isChecked = false
        }
    }
}
