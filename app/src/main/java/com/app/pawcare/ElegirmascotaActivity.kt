package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ElegirmascotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Permite que la actividad sea a pantalla completa
        setContentView(R.layout.activity_elegirmascota)

        // Configuración de insets para permitir la visualización de contenido sin interferir con las barras del sistema (opcional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el ImageView imageView2
        val imageView2: ImageView = findViewById(R.id.imageView2)

        // Configurar el listener para el clic en la imagen
        imageView2.setOnClickListener {
            // Crear el Intent para abrir SolicitudAlojamientoActivity
            val intent = Intent(this, SolicitudAlojamientoActivity::class.java)
            startActivity(intent) // Iniciar la actividad
        }
    }
}
