package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PaseadoresActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CuidadoresAdapter
    private val cuidadoresList = mutableListOf<Cuidador>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_paseadores)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerCuidadores)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CuidadoresAdapter(cuidadoresList, "paseador", this) // Se usa "paseador" como tipo
        recyclerView.adapter = adapter

        // Botón regresar
        findViewById<ImageView>(R.id.btn_regresar).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            finish()
        }

        // Cargar cuidadores que ofrecen el servicio "walker"
        cargarCuidadoresWalker()
    }

    private fun cargarCuidadoresWalker() {
        db.collection("caregivers")
            .whereArrayContains("services", "walker")
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(this, "No se encontraron paseadores disponibles", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                cuidadoresList.clear()

                for (doc in documentos) {
                    val userId = doc.id
                    val precio = doc.getDouble("hourlyRate") ?: 0.0

                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val nombre = userDoc.getString("username") ?: "Sin nombre"
                            val ubicacion = userDoc.getString("location") ?: "Sin ubicación"
                            val cuidador = Cuidador(nombre, ubicacion, "Precio: $${precio.toInt()} / hora")

                            cuidadoresList.add(cuidador)
                            adapter.notifyItemInserted(cuidadoresList.size - 1)
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar paseadores", Toast.LENGTH_SHORT).show()
            }
    }
}
