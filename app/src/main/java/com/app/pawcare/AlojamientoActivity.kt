package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AlojamientoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var nombreTextView: TextView
    private lateinit var ubicacionTextView: TextView
    private lateinit var precioTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alojamiento)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        nombreTextView = findViewById(R.id.textView18)
        ubicacionTextView = findViewById(R.id.textView19)
        precioTextView = findViewById(R.id.textView24)

        // Botón regresar
        findViewById<ImageView>(R.id.btn_regresar3).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            finish()
        }

        // Botón reservar
        findViewById<ImageView>(R.id.btn_reservar).setOnClickListener {
            startActivity(Intent(this, SolicitudAlojamientoActivity::class.java))
        }

        cargarCuidadoresConAlojamiento()
    }

    private fun cargarCuidadoresConAlojamiento() {
        db.collection("caregivers")
            .whereArrayContains("services", "boarding")
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(this, "No se encontraron cuidadores con alojamiento", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val primerCuidador = documentos.first()
                val userId = primerCuidador.id
                val precio = primerCuidador.getDouble("hourlyRate") ?: 0.0

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val nombre = userDoc.getString("username") ?: "Sin nombre"
                        val ubicacion = userDoc.getString("location") ?: "Sin ubicación"

                        nombreTextView.text = nombre
                        ubicacionTextView.text = ubicacion
                        precioTextView.text = "Precio: $${precio.toInt()} / hora"
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar cuidadores", Toast.LENGTH_SHORT).show()
            }
    }
}
