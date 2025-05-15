package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PerfilDuenoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var mascotaAdapter: MascotaAdapter
    private lateinit var rvMascotas: RecyclerView
    private lateinit var tvNoMascotas: TextView
    private lateinit var imgProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_dueno)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        val tvNombre = findViewById<TextView>(R.id.textView35)
        val tvUbicacion = findViewById<TextView>(R.id.textView34)
        imgProfile = findViewById(R.id.imageView20)
        rvMascotas = findViewById(R.id.rvMascotas)
        tvNoMascotas = findViewById(R.id.tvNoMascotas)

        // Configurar RecyclerView
        rvMascotas.layoutManager = LinearLayoutManager(this)
        mascotaAdapter = MascotaAdapter(emptyList()) { mascota ->
            val intent = Intent(this, EditarMascota_Activity::class.java)
            intent.putExtra("petId", mascota.petId)
            startActivity(intent)
        }
        rvMascotas.adapter = mascotaAdapter

        // Obtener usuario actual
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar datos del usuario y sus mascotas
        cargarDatosUsuario(userId, tvNombre, tvUbicacion)
        cargarMascotas(userId)

        // Configurar listeners de los botones
        configurarListeners()
    }

    private fun cargarDatosUsuario(userId: String, tvNombre: TextView, tvUbicacion: TextView) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    tvNombre.text = document.getString("username") ?: "Nombre no disponible"
                    tvUbicacion.text = document.getString("location") ?: "Ubicación no disponible"

                    // Cargar imagen de perfil
                    document.getString("profileImage")?.takeIf { it.isNotEmpty() }?.let { url ->
                        Glide.with(this)
                            .load(url)
                            .circleCrop()
                            .into(imgProfile)
                    }
                } else {
                    Toast.makeText(this, "Documento de usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener los datos del perfil", Toast.LENGTH_SHORT).show()
                Log.e("PerfilDueno", "Error al cargar usuario", it)
            }
    }

    private fun cargarMascotas(userId: String) {
        db.collection("pets") // o "pets" según tu colección
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val mascotas = result.map { document ->
                    Mascota(
                        petId = document.id,
                        name = document.getString("name") ?: "Sin nombre",
                        species = document.getString("species") ?: "No especificado",
                        breed = document.getString("breed") ?: "No especificado",
                        sex = document.getString("sex") ?: "No especificado",
                        birthDate = document.getString("birthDate"),
                        weight = document.getDouble("weight")?.toFloat() ?: 0f,
                        diseases = document.get("diseases") as? List<String> ?: emptyList(),
                        allergies = document.get("allergies") as? List<String> ?: emptyList(),
                        profileImage = document.getString("profileImage"),
                        userId = document.getString("userId") ?: userId
                    )
                }.also {
                    Log.d("Mascotas", "Mascotas encontradas: ${it.size}")
                }

                if (mascotas.isEmpty()) {
                    tvNoMascotas.visibility = View.VISIBLE
                    rvMascotas.visibility = View.GONE
                } else {
                    tvNoMascotas.visibility = View.GONE
                    rvMascotas.visibility = View.VISIBLE
                    mascotaAdapter.updateList(mascotas)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show()
                Log.e("PerfilDueno", "Error al cargar mascotas", exception)
                tvNoMascotas.visibility = View.VISIBLE
                rvMascotas.visibility = View.GONE
            }
    }

    private fun configurarListeners() {
        // Botón para ir a Configuración
        findViewById<ImageView>(R.id.imgConfiguracion).setOnClickListener {
            startActivity(Intent(this, Configuracion::class.java))
        }

        // Volver a inicio
        findViewById<ImageView>(R.id.btnhome14).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            finish()
        }

        // Agregar mascota
        findViewById<AppCompatButton>(R.id.btnAgregarm).setOnClickListener {
            startActivity(Intent(this, SelecionarMascota_Activity::class.java))
        }
    }

    // Clase de modelo de datos Mascota actualizada
    data class Mascota(
        val petId: String = "",
        val name: String = "",
        val species: String = "",
        val breed: String = "",
        val sex: String = "",
        val birthDate: String? = null,
        val weight: Float = 0f,
        val diseases: List<String> = emptyList(),
        val allergies: List<String> = emptyList(),
        val profileImage: String? = null,
        val userId: String = ""
    ) {
        // Función para calcular la edad aproximada
        fun getEdadAproximada(): Int {
            return birthDate?.let {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val birthDate = sdf.parse(it)
                    val now = Calendar.getInstance()
                    val birth = Calendar.getInstance().apply { time = birthDate }

                    var edad = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                    if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                        edad--
                    }
                    edad
                } catch (e: Exception) {
                    Log.e("Mascota", "Error al calcular edad", e)
                    0
                }
            } ?: 0
        }
    }

    // Adaptador actualizado para el RecyclerView
    class MascotaAdapter(
        private var mascotas: List<Mascota>,
        private val onEditClick: (Mascota) -> Unit
    ) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

        fun updateList(newList: List<Mascota>) {
            mascotas = newList
            notifyDataSetChanged()
        }

        class MascotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tv_mis_mascotas)
            val tvEspecie: TextView = view.findViewById(R.id.textView29)
            val tvEdad: TextView = view.findViewById(R.id.textView26)
            val tvSexo: TextView = view.findViewById(R.id.textView30)
            val tvPeso: TextView = view.findViewById(R.id.textView31)
            val tvRaza: TextView = view.findViewById(R.id.textView32)
            val tvCuidados: TextView = view.findViewById(R.id.textView25)
            val imgMascota: ImageView = view.findViewById(R.id.img_mascota)
            val btnEditar: TextView = view.findViewById(R.id.textView40)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mascota, parent, false)
            return MascotaViewHolder(view)
        }

        override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
            val mascota = mascotas[position]

            holder.tvNombre.text = mascota.name
            holder.tvEspecie.text = mascota.species
            holder.tvEdad.text = "${mascota.getEdadAproximada()} años"
            holder.tvSexo.text = mascota.sex
            holder.tvPeso.text = "%.1f kg".format(mascota.weight)
            holder.tvRaza.text = mascota.breed
            holder.tvCuidados.text = when {
                mascota.diseases.isNotEmpty() -> mascota.diseases.joinToString(", ")
                mascota.allergies.isNotEmpty() -> "Alergias: ${mascota.allergies.joinToString(", ")}"
                else -> "Ninguno"
            }

            // Cargar imagen de la mascota
            mascota.profileImage?.takeIf { it.isNotEmpty() }?.let { url ->
                Glide.with(holder.itemView.context)
                    .load(url)
                    .circleCrop()
                    .into(holder.imgMascota)
            } ?: holder.imgMascota.setImageResource(R.drawable.circulo_perfil)

            holder.btnEditar.setOnClickListener {
                onEditClick(mascota)
            }
        }

        override fun getItemCount() = mascotas.size
    }
}
