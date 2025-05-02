package com.app.pawcare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditarPerfilDueno : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_dueno)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Botón para volver a Configuraciondueno
        val btnBack = findViewById<ImageView>(R.id.imageView43)
        btnBack.setOnClickListener {
            val intent = Intent(this, Configuracion::class.java)
            startActivity(intent)
            finish()
        }

        // ✅ Botón agregar mascota
        val btnAgregarm = findViewById<ImageView>(R.id.btnAgregarm)
        btnAgregarm.setOnClickListener {
            startActivity(Intent(this, SelecionarMascota_Activity::class.java))
        }

    }
}
