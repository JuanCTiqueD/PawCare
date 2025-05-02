package com.app.pawcare

import android.app.DatePickerDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.android.gms.tasks.Tasks
import java.util.Calendar

class EditarPerfilCuidador_Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_perro)

        val fechaNacimiento = findViewById<EditText>(R.id.Fechanacer)
        fechaNacimiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val fecha = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    fechaNacimiento.setText(fecha)
                },
                year, month, day
            )

            datePickerDialog.show()
        }

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEspecie = findViewById<EditText>(R.id.etEspecie)
        val etSexo = findViewById<EditText>(R.id.etSexo)
        val etPeso = findViewById<EditText>(R.id.etPeso)
        val etVacunas = findViewById<EditText>(R.id.etVacunas)
        val etAlergias = findViewById<EditText>(R.id.etAlergias)
        val etCondicion = findViewById<EditText>(R.id.etCondicion)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            val nombre = etNombre.text.toString().trim()
            val especie = etEspecie.text.toString().trim()
            val sexo = etSexo.text.toString().trim()
            val peso = etPeso.text.toString().trim().toFloatOrNull() ?: 0f
            val fechaNac = fechaNacimiento.text.toString().trim()
            val enfermedades = etCondicion.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val alergias = etAlergias.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val progressDialog = mostrarProgressDialog("Guardando mascota...")

            val nuevaMascota = hashMapOf(
                "userId" to userId,
                "name" to nombre,
                "species" to especie,
                "sex" to sexo,
                "weight" to peso,
                "birthDate" to fechaNac,
                "diseases" to enfermedades,
                "allergies" to alergias,
                "breed" to "Desconocida",
                "profileImage" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "lastModifiedAt" to FieldValue.serverTimestamp()
            )

            db.collection("pets")
                .add(nuevaMascota)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Mascota registrada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
