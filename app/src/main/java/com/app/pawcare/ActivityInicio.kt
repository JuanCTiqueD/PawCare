package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityInicio : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        val btnPaseo = findViewById<ImageView>(R.id.btn_paseo)
        val btnAlojamiento = findViewById<ImageView>(R.id.btn_alojamiento)
        val btnEscuela = findViewById<ImageView>(R.id.btn_escuela)
        val btnPeluqueria = findViewById<ImageView>(R.id.btn_peluqueria)
        val btnPerfil = findViewById<ImageView>(R.id.btnperfil)
        val tvNombreUser = findViewById<TextView>(R.id.Nombre_user)

        // ✅ Obtener el nombre del usuario desde Firestore
        val currentUser = auth.currentUser
        currentUser?.let {
            db.collection("users")
                .document(it.uid)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username")
                    tvNombreUser.text = "Bienvenido, $username!"
                }
                .addOnFailureListener {
                    tvNombreUser.text = "Bienvenido"
                }
        }

        btnPaseo.setOnClickListener {
            startActivity(Intent(this, PaseadoresActivity::class.java))
        }

        btnAlojamiento.setOnClickListener {
            startActivity(Intent(this, AlojamientoActivity::class.java))
        }

        btnEscuela.setOnClickListener {
            startActivity(Intent(this, EscuelaActivity::class.java))
        }

        btnPeluqueria.setOnClickListener {
            startActivity(Intent(this, PeluqueriaActivity::class.java))
        }

        // ✅ Ir a la pantalla de perfil al hacer clic en el botón de perfil
        btnPerfil.setOnClickListener {
            val intent = Intent(this, PerfilDuenoActivity::class.java)
            startActivity(intent)
        }


    }
}
