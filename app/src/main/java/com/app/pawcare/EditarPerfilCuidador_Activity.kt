package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditarPerfilCuidador_Activity : AppCompatActivity() {

    private lateinit var anadirservicio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil_cuidador)

        val btnAgregarServicio: ImageView = findViewById(R.id.btnAgregarServicio)
        val imageViewEditar: ImageView = findViewById(R.id.imageView67) // ← botón que te llevará a Configuración

        anadirservicio = findViewById(R.id.anadirservicio)

        btnAgregarServicio.setOnClickListener { view ->
            mostrarMenuServicios(view)
        }

        // Navegación a ConfiguracionCuidador_Activity
        imageViewEditar.setOnClickListener {
            val intent = Intent(this, ConfiguracionCuidador_Activity::class.java)
            startActivity(intent)
        }
    }

    private fun mostrarMenuServicios(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.servicios_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            val servicio = when (item.itemId) {
                R.id.servicio_paseador -> "Paseador de mascotas"
                R.id.servicio_cuidador -> "Cuidador de mascotas"
                R.id.servicio_escuela -> "Escuela de adiestramiento"
                R.id.servicio_spa -> "Spa para mascotas"
                else -> null
            }

            servicio?.let {
                mostrarSeleccion(it)
                true
            } ?: false
        }

        popup.show()
    }

    private fun mostrarSeleccion(servicio: String) {
        anadirservicio.text = servicio
        Toast.makeText(this, "Seleccionaste: $servicio", Toast.LENGTH_SHORT).show()
    }
}
