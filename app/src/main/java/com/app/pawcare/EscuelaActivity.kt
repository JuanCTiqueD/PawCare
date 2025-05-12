package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EscuelaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_escuela)

        // Configuración de insets para que el contenido no quede cubierto por las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de regresar (fuera del listener)
        val btnRegresar = findViewById<ImageView>(R.id.btn_regresar2)
        btnRegresar.setOnClickListener {
            val intent = Intent(this, ActivityInicio::class.java)
            startActivity(intent)
            finish() // Opcional: evita que esta pantalla quede en el historial
        }

        // Botón de reservar (ImageView)
        val btnReservar = findViewById<ImageView>(R.id.btn_reservar)
        btnReservar.setOnClickListener {
            // Crear el Intent para abrir solicitud escuela
            val intent = Intent(this, SolicitudEscuelaActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }
    }
}