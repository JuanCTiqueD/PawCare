package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SolicitudAlojamientoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitud_alojamiento)

        // Obtener el botón btnSolicitud2
        val btnSolicitud2: Button = findViewById(R.id.btnSolicitud2)

        // Configurar el listener para el clic del botón
        btnSolicitud2.setOnClickListener {
            // Crear el Intent para abrir solicitud
            val intent = Intent(this, HacerSolicitudActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }

        // Obtener el ImageView imageView8
        val imageView8: ImageView = findViewById(R.id.imageView8)

        // Configurar el listener para el clic en la imagen
        imageView8.setOnClickListener {
            // Crear el Intent para abrir AlojamientoActivity
            val intent = Intent(this, AlojamientoActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }
    }
}
