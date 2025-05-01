package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class Configuracion : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        // Inicializar Firebase Auth
        auth = Firebase.auth

        // 1. Botón de Cerrar Sesión
        val tvLogout = findViewById<TextView>(R.id.btn_logout)
        tvLogout.setOnClickListener {
            cerrarSesion()
        }

        // 2. Botón de Regresar
        val imageBack = findViewById<ImageView>(R.id.imageView36)
        imageBack.setOnClickListener {
            finish() // Solo cierra esta actividad, sin reiniciar PerfilDuenoActivity
        }

        // 3. Botón Editar Perfil (existente)
        val editarPerfil = findViewById<TextView>(R.id.edit_profile)
        editarPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilDueno::class.java))
        }

        // 4. Configurar botón Eliminar Cuenta
        val btnDelete = findViewById<Button>(R.id.btn_delete)
        btnDelete.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun cerrarSesion() {
        auth.signOut() // Cierra sesión en Firebase

        // Redirige al Login y limpia el back stack
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro? Todos tus datos (mascotas, reservas, etc.) se borrarán permanentemente.")
            .setPositiveButton("Eliminar") { _, _ ->
                if (auth.currentUser != null) {
                    // Solicitar autenticación reciente primero
                    solicitarReautenticacion()
                }
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun solicitarReautenticacion() {
        val user = auth.currentUser ?: return

        // 1. Mostrar diálogo para ingresar contraseña
        val passwordDialog = AlertDialog.Builder(this)
            .setTitle("Verificación de seguridad")
            .setMessage("Por favor ingresa tu contraseña para confirmar:")
            .setView(R.layout.dialog_password) // Crea este layout (abajo)
            .setPositiveButton("Confirmar") { _, _ ->
                val password = findViewById<EditText>(R.id.et_password).text.toString()
                val credential = EmailAuthProvider.getCredential(user.email!!, password)

                // 2. Reautenticar
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        eliminarCuentaCompleta()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        passwordDialog.show()
    }

    private fun eliminarCuentaCompleta() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        // 1. Eliminar datos de Firestore (usuarios + mascotas relacionadas)
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                // 2. Eliminar mascotas (opcional)
                db.collection("pets")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { pets ->
                        pets.forEach { it.reference.delete() }
                    }

                // 3. Eliminar imágenes de perfil (opcional)
                storage.reference.child("profile_images/$userId.jpg").delete()

                // 4. Eliminar cuenta de Auth
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                        redirigirALogin()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirigirALogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}