package com.app.pawcare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class EditarMascota_Activity : AppCompatActivity() {

    private val IMAGE_PICK_CODE = 1001
    private var imageUri: Uri? = null
    private lateinit var storageRef: StorageReference
    private lateinit var imageViewPerfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_mascota)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAgregarImagen = findViewById<Button>(R.id.btnAgregarImagen)
        imageViewPerfil = findViewById(R.id.imageView38)
        storageRef = FirebaseStorage.getInstance().reference.child("mascotas")

        btnAgregarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        cargarImagenActual()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageViewPerfil.setImageURI(imageUri)
            imageUri?.let { subirImagenAFirebase(it) }
        }
    }

    private fun subirImagenAFirebase(uri: Uri) {
        val fileRef = storageRef.child("${UUID.randomUUID()}.jpg")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    guardarUrlEnFirestore(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarUrlEnFirestore(url: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val mascotaRef = FirebaseFirestore.getInstance().collection("mascotas").document(uid)

        mascotaRef.update("imagenUrl", url)
            .addOnSuccessListener {
                Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo actualizar la imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarImagenActual() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val mascotaRef = FirebaseFirestore.getInstance().collection("mascotas").document(uid)

        mascotaRef.get().addOnSuccessListener { document ->
            val imageUrl = document.getString("imagenUrl")
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this).load(imageUrl).into(imageViewPerfil)
            }
        }
    }
}
