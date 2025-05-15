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

class AlojamientoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CuidadoresAdapter
    private val listaCuidadores = mutableListOf<Cuidador>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alojamiento)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerCuidadores)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CuidadoresAdapter(listaCuidadores)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.btn_regresar3).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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

// Clase de datos para el cuidador
data class Cuidador(val nombre: String, val ubicacion: String, val precio: String)

// Adaptador para el RecyclerView
class CuidadoresAdapter(private val cuidadores: List<Cuidador>) : RecyclerView.Adapter<CuidadoresAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val nombre = itemView.findViewById<android.widget.TextView>(R.id.tv_nombre)
        val ubicacion = itemView.findViewById<android.widget.TextView>(R.id.tv_ubicacion)
        val precio = itemView.findViewById<android.widget.TextView>(R.id.tv_precio)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_cuidador, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cuidador = cuidadores[position]
        holder.nombre.text = cuidador.nombre
        holder.ubicacion.text = cuidador.ubicacion
        holder.precio.text = cuidador.precio
    }

    override fun getItemCount(): Int = cuidadores.size
}
