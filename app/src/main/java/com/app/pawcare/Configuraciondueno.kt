package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Configuraciondueno : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuraciondueno)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Regresar a PerfilDuenoActivity
        val imageBack = findViewById<ImageView>(R.id.imageView36)
        imageBack.setOnClickListener {
            val intent = Intent(this, PerfilDuenoActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ✅ Ir a EditarPerfilDueno
        val editarPerfil = findViewById<TextView>(R.id.editarperfil)
        editarPerfil.setOnClickListener {
            val intent = Intent(this, EditarPerfilDueno::class.java)
            startActivity(intent)
        }
    }
}
