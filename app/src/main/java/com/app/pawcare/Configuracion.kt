package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Configuracion : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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
}