package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilDueno : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_dueno)

        val etNombre = findViewById<EditText>(R.id.etNombre2)
        val etCorreo = findViewById<EditText>(R.id.etNombre4)
        val etUbicacion = findViewById<EditText>(R.id.etNombre3)
        val etNumero = findViewById<EditText>(R.id.etNumero)
        val btnBack = findViewById<ImageView>(R.id.imageView43)
        val btnGuardarCambios = findViewById<Button>(R.id.btnguardarcambios)

        // 🔙 Botón atrás que usa la pila del sistema
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 📥 Cargar datos actuales del usuario
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                etNombre.setText(doc.getString("username") ?: "")
                etCorreo.setText(doc.getString("email") ?: "")
                etUbicacion.setText(doc.getString("location") ?: "")
                etNumero.setText(doc.getString("contactNumber") ?: "")
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo cargar tu información", Toast.LENGTH_SHORT).show()
            }

        // 💾 Guardar cambios al presionar el botón
        btnGuardarCambios.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoCorreo = etCorreo.text.toString().trim()
            val nuevaUbicacion = etUbicacion.text.toString().trim()
            val nuevoNumero = etNumero.text.toString().trim()

            val cambios = mapOf(
                "username" to nuevoNombre,
                "email" to nuevoCorreo,
                "location" to nuevaUbicacion,
                "contactNumber" to nuevoNumero,
                "lastModifiedAt" to FieldValue.serverTimestamp()
            )

            val progressDialog = mostrarProgressDialog("Actualizando perfil...")

            db.collection("users").document(userId).update(cambios)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PerfilDuenoActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarProgressDialog(mensaje: String): AlertDialog {
        val view = layoutInflater.inflate(R.layout.dialog_progress, null)
        view.findViewById<TextView>(R.id.tv_progress_message).text = mensaje
        return AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()
            .also { it.show() }
    }
}

