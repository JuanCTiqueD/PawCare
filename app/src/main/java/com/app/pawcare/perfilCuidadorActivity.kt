package com.app.pawcare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.pm.PackageManager

class perfilCuidadorActivity : AppCompatActivity() {

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

        // Otros botones
        val btnPerrificado = findViewById<ImageView>(R.id.perrificado)
        btnPerrificado.setOnClickListener {
            startActivity(Intent(this, PerrificadoCuidador_Activity::class.java))
        }

        val imageBack = findViewById<ImageView>(R.id.btnhome17)
        imageBack.setOnClickListener {
            startActivity(Intent(this, CuidadorActivity::class.java))
        }

        val btn_config_cuidador = findViewById<ImageView>(R.id.imgConfiguracion)
        btn_config_cuidador.setOnClickListener {
            startActivity(Intent(this, ConfiguracionCuidador_Activity::class.java))
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
