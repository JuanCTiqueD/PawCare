package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditarPerfilCuidador_Activity : AppCompatActivity() {

    private lateinit var anadirservicio: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil_cuidador)

        val uid = auth.currentUser?.uid ?: return

        val imageViewEditar: ImageView = findViewById(R.id.imageView67)
        val btnAgregarServicio: ImageView = findViewById(R.id.btnAgregarServicio)
        anadirservicio = findViewById(R.id.anadirservicio)

        val etNombre = findViewById<EditText>(R.id.nombreusuario)
        val etCorreo = findViewById<EditText>(R.id.correo)
        val etNumero = findViewById<EditText>(R.id.etNumero2)
        val etDescripcion = findViewById<EditText>(R.id.descripcion)
        val etExperiencia = findViewById<EditText>(R.id.etExperiencia)
        val etCertificaciones = findViewById<EditText>(R.id.certificaciones)
        val etTamanos = findViewById<EditText>(R.id.tamanomascotas)
        val etPrecio = findViewById<EditText>(R.id.precio)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCuidador)

        // Cargar información previa si existe
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            etNombre.setText(doc.getString("username") ?: "")
            etCorreo.setText(doc.getString("email") ?: "")
            etNumero.setText(doc.getString("contactNumber") ?: "")
        }

        db.collection("caregivers").document(uid).get().addOnSuccessListener { doc ->
            etDescripcion.setText(doc.getString("description") ?: "")
            etExperiencia.setText(doc.getString("experience") ?: "")
            etCertificaciones.setText((doc.get("certifications") as? List<*>)?.joinToString(", ") ?: "")
            etTamanos.setText((doc.get("acceptedPetSizes") as? Map<*, *>)?.let {
                "${it["min"]} - ${it["max"]} kg"
            } ?: "")
            etPrecio.setText(doc.getDouble("hourlyRate")?.toString() ?: "")
            anadirservicio.text = (doc.get("services") as? List<*>)?.joinToString(", ") ?: ""
        }

        btnAgregarServicio.setOnClickListener { view -> mostrarMenuServicios(view) }

        imageViewEditar.setOnClickListener {
            startActivity(Intent(this, ConfiguracionCuidador_Activity::class.java))
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val experiencia = etExperiencia.text.toString().trim()
            val certificaciones = etCertificaciones.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val servicios = anadirservicio.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val precio = etPrecio.text.toString().toFloatOrNull() ?: 0f

            val tamanos = etTamanos.text.toString()
            val tamanosSplit = tamanos.split("-").map { it.trim().removeSuffix("kg").toFloatOrNull() }
            val acceptedPetSizes = mapOf(
                "min" to (tamanosSplit.getOrNull(0) ?: 0f),
                "max" to (tamanosSplit.getOrNull(1) ?: 0f)
            )
            val numero = etNumero.text.toString().trim()

            val userMap = mapOf(
                "username" to nombre,
                "email" to correo,
                "contactNumber" to numero,
                "lastModifiedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            val caregiverMap = mapOf(
                "description" to descripcion,
                "experience" to experiencia,
                "services" to servicios,
                "hourlyRate" to precio,
                "certifications" to certificaciones,
                "acceptedPetSizes" to acceptedPetSizes,
                "lastModifiedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            // Guardar en Firestore (crear o actualizar)
            db.collection("users").document(uid).set(userMap, SetOptions.merge())
                .continueWithTask {
                    db.collection("caregivers").document(uid).set(caregiverMap, SetOptions.merge())
                }
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    redirigirAPerfil()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarMenuServicios(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.servicios_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            val servicio = when (item.itemId) {
                R.id.servicio_paseador -> "Paseador de mascotas"
                R.id.servicio_cuidador -> "Cuidador de mascotas"
                R.id.servicio_escuela -> "Escuela de adiestramiento"
                R.id.servicio_spa -> "Spa para mascotas"
                else -> null
            }

            servicio?.let {
                mostrarSeleccion(it)
                true
            } ?: false
        }

        popup.show()
    }

    private fun mostrarSeleccion(servicio: String) {
        val serviciosActuales = anadirservicio.text.toString()
        val listaServicios = serviciosActuales.split(",").map { it.trim() }.toMutableList()

        if (!listaServicios.contains(servicio)) {
            listaServicios.add(servicio)
            anadirservicio.text = listaServicios.joinToString(", ")
        }

        Toast.makeText(this, "Servicio añadido: $servicio", Toast.LENGTH_SHORT).show()
    }

    private fun redirigirAPerfil() {
        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, perfilCuidadorActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}
