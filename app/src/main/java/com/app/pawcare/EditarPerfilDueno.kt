package com.app.pawcare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditarPerfilDueno : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_dueno)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Botón para volver a Configuraciondueno
        val btnBack = findViewById<ImageView>(R.id.imageView43)
        btnBack.setOnClickListener {
            val intent = Intent(this, Configuracion::class.java)
            startActivity(intent)
            finish()
        }

        val btnGuardarCambios = findViewById<Button>(R.id.btnguardarcambios)
        btnGuardarCambios.setOnClickListener {
            // Aquí pones lo que quieras que haga cuando el usuario guarde los cambios.
            // Por ejemplo, podrías mostrar un mensaje:
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()

            // O regresar a la pantalla anterior:
            val intent = Intent(this, PerfilDuenoActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
