package com.app.pawcare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class perfilCuidadorActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_cuidador)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Perrificado

        val btnPerrificado = findViewById<TextView>(R.id.perrificado)
        btnPerrificado.setOnClickListener { startActivity(Intent(this,PerrificadoCuidador_Activity::class.java))
        }

        // Configuración
        val btn_config_cuidador = findViewById<TextView>(R.id.imgConfiguracion)
        btn_config_cuidador.setOnClickListener { startActivity(Intent(this,ConfiguracionCuidador_Activity::class.java))
        }

    }
}