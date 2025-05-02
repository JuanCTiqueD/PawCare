package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PerfilDuenoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_dueno)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navegación a Configuraciondueno
        val imgConfiguracion = findViewById<ImageView>(R.id.imgConfiguracion)
        imgConfiguracion.setOnClickListener {
            val intent = Intent(this, Configuraciondueno::class.java)
            startActivity(intent)
        }

        // Volver a ActivityInicio al presionar btnhome14
        val btnHome = findViewById<ImageView>(R.id.btnhome14)
        btnHome.setOnClickListener {
            val intent = Intent(this, ActivityInicio::class.java)
            startActivity(intent)
            finish()
        }

        // Ir a SelecionarMascota_Activity al presionar btnAgregarm
        val btnAgregarMascota = findViewById<AppCompatButton>(R.id.btnAgregarm)
        btnAgregarMascota.setOnClickListener {
            val intent = Intent(this, SelecionarMascota_Activity::class.java)
            startActivity(intent)
        }
    }
}
