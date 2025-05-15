package com.app.pawcare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.content.pm.PackageManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class perfilCuidadorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var imageView53: ImageView
    private val REQUEST_CODE_IMAGE_PICK = 100
    private val REQUEST_CODE_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_cuidador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView53 = findViewById(R.id.imageView53)
        imageView53.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        // Botones
        findViewById<ImageView>(R.id.perrificado).setOnClickListener {
            startActivity(Intent(this, PerrificadoCuidador_Activity::class.java))
        }

        findViewById<ImageView>(R.id.btnhome17).setOnClickListener {
            startActivity(Intent(this, CuidadorActivity::class.java))
        }

        findViewById<ImageView>(R.id.imgConfiguracion).setOnClickListener {
            startActivity(Intent(this, ConfiguracionCuidador_Activity::class.java))
        }

        cargarDatosPerfil()
    }

    private fun cargarDatosPerfil() {
        val userId = auth.currentUser?.uid ?: return

        val tvNombre = findViewById<TextView>(R.id.btn_logout2)
        val tvUbicacion = findViewById<TextView>(R.id.textView41)
        val tvDescripcion = findViewById<TextView>(R.id.textView39)
        val tvExperiencia = findViewById<TextView>(R.id.textView43)
        val tvCertificaciones = findViewById<TextView>(R.id.textView42)
        val tvPrecio = findViewById<TextView>(R.id.textView38)
        val tvTamañoAceptado = findViewById<TextView>(R.id.textView44)
        val imgPerfil = findViewById<ImageView>(R.id.imageView53)

        // Obtener datos del usuario
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                tvNombre.text = userDoc.getString("username") ?: "Sin nombre"
                tvUbicacion.text = userDoc.getString("location") ?: "Ubicación desconocida"
                userDoc.getString("profileImage")?.let { url ->
                    if (url.isNotBlank()) {
                        Glide.with(this).load(url).circleCrop().into(imgPerfil)
                    }
                }
            }

        // Obtener datos del cuidador
        db.collection("caregivers").document(userId).get()
            .addOnSuccessListener { caregiverDoc ->
                tvDescripcion.text = caregiverDoc.getString("description") ?: "Sin descripción"
                tvExperiencia.text = caregiverDoc.getString("experience") ?: "0 años"

                val precio = caregiverDoc.getDouble("hourlyRate") ?: 0.0
                tvPrecio.text = "$${precio}/Noche"

                val certificaciones = caregiverDoc.get("certifications") as? List<*> ?: emptyList<Any>()
                tvCertificaciones.text = certificaciones.joinToString("\n") { it.toString() }

                val acceptedSize = caregiverDoc.get("acceptedPetSizes") as? Map<*, *>
                val min = acceptedSize?.get("min")?.toString()?.toFloatOrNull() ?: 0f
                val max = acceptedSize?.get("max")?.toString()?.toFloatOrNull() ?: 0f
                tvTamañoAceptado.text = "${min.toInt()}-${max.toInt()} kg"
            }
    }

    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                openGallery()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            imageView53.setImageURI(selectedImageUri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
    }
}