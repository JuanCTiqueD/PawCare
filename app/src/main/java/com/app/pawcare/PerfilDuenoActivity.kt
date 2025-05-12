package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilDuenoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_dueno)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 Referencias a los campos de texto
        val tvNombre = findViewById<TextView>(R.id.textView35)
        val tvUbicacion = findViewById<TextView>(R.id.textView34)

        // 🔹 Obtener usuario actual
        val userId = auth.currentUser?.uid ?: return

        // 🔹 Traer datos del usuario desde Firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                tvNombre.text = document.getString("username") ?: "Nombre no disponible"
                tvUbicacion.text = document.getString("location") ?: "Ubicación no disponible"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener los datos del perfil", Toast.LENGTH_SHORT).show()
            }

        // 🔙 Botón para ir a Configuración
        val imgConfiguracion = findViewById<ImageView>(R.id.imgConfiguracion)
        imgConfiguracion.setOnClickListener {
            startActivity(Intent(this, Configuracion::class.java))
        }

        // 🏠 Volver a inicio
        val btnHome = findViewById<ImageView>(R.id.btnhome14)
        btnHome.setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            finish()
        }

        // ➕ Agregar mascota
        val btnAgregarMascota = findViewById<AppCompatButton>(R.id.btnAgregarm)
        btnAgregarMascota.setOnClickListener {
            startActivity(Intent(this, SelecionarMascota_Activity::class.java))
        }
    }
}
