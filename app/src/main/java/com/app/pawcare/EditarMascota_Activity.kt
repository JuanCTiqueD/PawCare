package com.app.pawcare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class EditarMascota_Activity : AppCompatActivity() {

    private val IMAGE_PICK_CODE = 1001
    private var imageUri: Uri? = null
    private lateinit var imageViewPerfil: ImageView
    private lateinit var storageRef: StorageReference
    private lateinit var petId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_mascota)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        petId = intent.getStringExtra("petId") ?: return
        storageRef = FirebaseStorage.getInstance().reference.child("pets")
        imageViewPerfil = findViewById(R.id.imageView38)

        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnAgregarImagen = findViewById<Button>(R.id.btnAgregarImagen)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar2)

        btnGuardar.setOnClickListener { guardarDatos() }
        btnAgregarImagen.setOnClickListener { seleccionarImagen() }
        btnEliminar.setOnClickListener { eliminarMascota() }

        cargarDatosMascota()
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageViewPerfil.setImageURI(imageUri)
        }
    }

    private fun cargarDatosMascota() {
        val mascotaRef = FirebaseFirestore.getInstance().collection("pets").document(petId)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        mascotaRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val ownerId = doc.getString("userId")
                if (ownerId != currentUserId) {
                    Toast.makeText(this, "No tienes permiso para editar esta mascota", Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }

                findViewById<EditText>(R.id.etNombre).setText(doc.getString("name"))
                findViewById<EditText>(R.id.etEspecie).setText(doc.getString("species"))
                findViewById<EditText>(R.id.etSexo).setText(doc.getString("sex"))
                findViewById<EditText>(R.id.etEdad).setText(doc.getString("birthDate"))
                findViewById<EditText>(R.id.etPeso).setText(doc.getDouble("weight")?.toString())
                findViewById<EditText>(R.id.etCondicion).setText((doc.get("diseases") as? List<*>)?.joinToString(", ") ?: "")
                findViewById<EditText>(R.id.etAlergias).setText((doc.get("allergies") as? List<*>)?.joinToString(", ") ?: "")
                findViewById<EditText>(R.id.etVacunas).setText((doc.get("vaccines") as? List<*>)?.joinToString(", ") ?: "") // <- vacunas

                val imageUrl = doc.getString("profileImage")
                if (!imageUrl.isNullOrBlank()) {
                    Glide.with(this).load(imageUrl).into(imageViewPerfil)
                }
            }
        }
    }

    private fun guardarDatos() {
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("pets").document(petId)

        val nombre = findViewById<EditText>(R.id.etNombre).text.toString()
        val especie = findViewById<EditText>(R.id.etEspecie).text.toString()
        val sexo = findViewById<EditText>(R.id.etSexo).text.toString()
        val fecha = findViewById<EditText>(R.id.etEdad).text.toString()
        val peso = findViewById<EditText>(R.id.etPeso).text.toString().toFloatOrNull() ?: 0f
        val enfermedades = findViewById<EditText>(R.id.etCondicion).text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val alergias = findViewById<EditText>(R.id.etAlergias).text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val vacunas = findViewById<EditText>(R.id.etVacunas).text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val data = hashMapOf(
            "name" to nombre,
            "species" to especie,
            "sex" to sexo,
            "birthDate" to fecha,
            "weight" to peso,
            "diseases" to enfermedades,
            "allergies" to alergias,
            "vaccines" to vacunas // <- vacunas
        )

        if (imageUri != null) {
            val fileRef = storageRef.child("${UUID.randomUUID()}.jpg")
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        data["profileImage"] = uri.toString()
                        ref.update(data as Map<String, Any>)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Datos actualizados con imagen", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                    }
                }
        } else {
            ref.update(data as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
        }
    }

    private fun eliminarMascota() {
        val db = FirebaseFirestore.getInstance()
        db.collection("pets").document(petId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar mascota", Toast.LENGTH_SHORT).show()
            }
    }
}
