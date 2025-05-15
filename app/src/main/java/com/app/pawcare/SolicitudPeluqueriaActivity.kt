package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SolicitudPeluqueriaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_solicitud_peluqueria)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnSolicitud4: Button = findViewById(R.id.btnSolicitud4)

        // Configurar el listener para el clic del botón
        btnSolicitud4.setOnClickListener {
            // Crear el Intent para abrir solicitud
            val intent = Intent(this, HacerSolicitudActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }

        // Botón de regresar (ImageView)
        val imageView25 = findViewById<ImageView>(R.id.imageView25)
        imageView25.setOnClickListener {
            // Crear el Intent para abrir peluqueria activity
            val intent = Intent(this, PeluqueriaActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }

    }
}