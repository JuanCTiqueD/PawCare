package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SelecionarMascota_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selecionar_mascota)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Declarar
        val btnPerfilperro = findViewById<ImageView>(R.id.flecha_perro)
        val btnPerfilGato = findViewById<ImageView>(R.id.flecha_gato)

        // ✅ Botón perfil perro
        btnPerfilperro.setOnClickListener {
            startActivity(Intent(this, PerfilPerro_Activity::class.java))
        }
        // ✅ Botón perfil gato
        btnPerfilGato.setOnClickListener {
            startActivity(Intent(this, PerfilGato_Activity::class.java))
        }
        // Botón de reservar (ImageView)
        val imageView14 = findViewById<ImageView>(R.id.imageView14)
        imageView14.setOnClickListener {
            // Crear el Intent para abrir solicitud peluqueria
            val intent = Intent(this, PerfilDuenoActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }
    }
}