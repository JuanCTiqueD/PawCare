package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore

class EscuelaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CuidadoresAdapter
    private val cuidadoresList = mutableListOf<Cuidador>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escuela)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerCuidadores)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CuidadoresAdapter(cuidadoresList, "escuela", this)
        recyclerView.adapter = adapter

        // Botón regresar
        findViewById<ImageView>(R.id.btn_regresar2).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            finish()
        }

        cargarCuidadoresTraining()
    }

    private fun cargarCuidadoresTraining() {
        db.collection("caregivers")
            .whereArrayContains("services", "training")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No se encontraron cuidadores con escuela", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                cuidadoresList.clear()
                val docs = snapshot.documents
                val tareas = docs.map { cuidadorDoc ->
                    val userId = cuidadorDoc.id
                    val precio = cuidadorDoc.getDouble("hourlyRate") ?: 0.0

                    db.collection("users").document(userId).get().continueWith { userTask ->
                        val userDoc = userTask.result
                        val nombre = userDoc?.getString("username") ?: "Sin nombre"
                        val ubicacion = userDoc?.getString("location") ?: "Sin ubicación"
                        val cuidador = Cuidador(nombre, ubicacion, precio.toString())
                        cuidadoresList.add(cuidador)
                    }
                }

                // Esperar todas las tareas
                Tasks.whenAllComplete(tareas)
                    .addOnSuccessListener {
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al procesar cuidadores", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar cuidadores", Toast.LENGTH_SHORT).show()
            }
    }
}
