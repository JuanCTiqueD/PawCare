package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class CuidadorActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cuidador_activity)

        // 3. Botón para direccionar a Perfil
        val btnperfil_cuidador = findViewById<ImageView>(R.id.btnperfil16)

        btnperfil_cuidador.setOnClickListener {
            startActivity(Intent(this, perfilCuidadorActivity::class.java))
        }

        val btnhuella13 =findViewById<ImageView>(R.id.btnhuella13)

        btnhuella13.setOnClickListener {
            val intent = Intent(this,HistorialSolicitudCuidador_Activity::class.java)
            startActivity(intent)
        }

    }
}