package com.app.pawcare

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PeluqueriaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CuidadoresAdapter
    private val listaCuidadores = mutableListOf<Cuidador>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peluqueria)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerCuidadores)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CuidadoresAdapter(listaCuidadores, "peluqueria", this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.btn_regresar4).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cargarCuidadoresPeluqueria()
    }

    private fun cargarCuidadoresPeluqueria() {
        db.collection("caregivers")
            .whereArrayContains("services", "grooming")
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(this, "No se encontraron cuidadores con peluquería", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                listaCuidadores.clear()

                for (doc in documentos) {
                    val userId = doc.id
                    val precio = doc.getDouble("hourlyRate") ?: 0.0

                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val nombre = userDoc.getString("username") ?: "Sin nombre"
                            val ubicacion = userDoc.getString("location") ?: "Sin ubicación"

                            listaCuidadores.add(Cuidador(nombre, ubicacion, "Precio: $${precio.toInt()} / hora"))
                            adapter.notifyItemInserted(listaCuidadores.size - 1)
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar cuidadores", Toast.LENGTH_SHORT).show()
            }
    }
}
