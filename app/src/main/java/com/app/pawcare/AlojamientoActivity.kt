package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlojamientoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alojamiento)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Botón de regresar (fuera del listener)
        val btnRegresar = findViewById<ImageView>(R.id.btn_regresar3)
        btnRegresar.setOnClickListener {
            val intent = Intent(this, ActivityInicio::class.java)
            startActivity(intent)
            finish() // Opcional: evita que esta pantalla quede en el historial
        }
        // Botón de home (ImageView)
        val btnHome = findViewById<ImageView>(R.id.btnhome3)
        btnHome.setOnClickListener {
            val intent = Intent(this, ActivityInicio::class.java)
            startActivity(intent)
            finish()
        }
        //Botón reservar (Image view)
        val btnReservar = findViewById<ImageView>(R.id.btn_reservar)
        btnReservar.setOnClickListener {
            val intent = Intent(this, SolicitudAlojamientoActivity::class.java)
            startActivity(intent)
        }

    }
}
