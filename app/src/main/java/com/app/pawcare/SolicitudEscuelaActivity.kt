package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SolicitudEscuelaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_solicitud_escuela)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets


        }
        val btnSolicitud3: Button = findViewById(R.id.btnSolicitud3)

        // Configurar el listener para el clic del botón
        btnSolicitud3.setOnClickListener {
            // Crear el Intent para abrir solicitud
            val intent = Intent(this, HacerSolicitudActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }


        // Obtener el ImageView imageView21
        val imageView21: ImageView = findViewById(R.id.imageView21)

        // Configurar el listener para el clic en la imagen
        imageView21.setOnClickListener {
            // Crear el Intent para abrir Escuelaq activity
            val intent = Intent(this, EscuelaActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }
    }
}