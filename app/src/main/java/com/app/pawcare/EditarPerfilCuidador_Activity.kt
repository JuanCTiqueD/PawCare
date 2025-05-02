package com.app.pawcare

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilCuidador_Activity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_cuidador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombre = findViewById<EditText>(R.id.etNombre5)
        val etNumero = findViewById<EditText>(R.id.etNumero2)
        val etCorreo = findViewById<EditText>(R.id.etNombre7)
        val etDescripcion = findViewById<EditText>(R.id.etNombre6)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCuidador)

        val userId = auth.currentUser?.uid ?: return

        // 🔹 Cargar datos actuales
        db.collection("users").document(userId).get()
            .addOnSuccessListener { docUser ->
                etNombre.setText(docUser.getString("username") ?: "")
                etCorreo.setText(docUser.getString("email") ?: "")
                etNumero.setText(docUser.getString("contactNumber") ?: "")
            }

        db.collection("caregivers").document(userId).get()
            .addOnSuccessListener { docCaregiver ->
                etDescripcion.setText(docCaregiver.getString("description") ?: "")
            }

        // 🔹 Guardar cambios
        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoCorreo = etCorreo.text.toString().trim()
            val nuevoNumero = etNumero.text.toString().trim()
            val nuevaDescripcion = etDescripcion.text.toString().trim()

            val progressDialog = mostrarProgressDialog("Guardando perfil...")

            val updatesUser = mapOf(
                "username" to nuevoNombre,
                "email" to nuevoCorreo,
                "contactNumber" to nuevoNumero,
                "lastModifiedAt" to FieldValue.serverTimestamp()
            )

            val updatesCaregiver = mapOf(
                "description" to nuevaDescripcion,
                "lastModifiedAt" to FieldValue.serverTimestamp(),
                "experience" to "5 años",  // quemado temporal
                "services" to listOf("Paseo", "Guardería"),
                "hourlyRate" to 20.0
            )

            val task1 = db.collection("users").document(userId).update(updatesUser)
            val task2 = db.collection("caregivers").document(userId).update(updatesCaregiver)

            Tasks.whenAllComplete(task1, task2)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_SHORT).show()
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
