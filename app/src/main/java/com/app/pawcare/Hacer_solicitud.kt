package com.app.pawcare

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class HacerSolicitudActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var etFechaInicio: EditText
    private lateinit var etFechaFin: EditText
    private lateinit var etHoraInicio: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var etUbicacion: EditText
    private lateinit var etServicio: EditText
    private lateinit var btnGuardar: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hacer_solicitud)

        // Asignación correcta a las variables globales
        etFechaInicio = findViewById(R.id.etFechaInicio)
        etFechaFin = findViewById(R.id.etFechaFinal)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etHoraFin = findViewById(R.id.etHoraFinal)
        etUbicacion = findViewById(R.id.etUbicacion)
        etServicio = findViewById(R.id.etServicio)
        btnGuardar = findViewById(R.id.btnGuardar)

        etFechaInicio.setOnClickListener { showDatePicker(etFechaInicio) }
        etFechaFin.setOnClickListener { showDatePicker(etFechaFin) }
        etHoraInicio.setOnClickListener { showTimePicker(etHoraInicio) }
        etHoraFin.setOnClickListener { showTimePicker(etHoraFin) }

        btnGuardar.setOnClickListener { guardarSolicitud() }
    }

    private fun showDatePicker(editText: EditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                editText.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: EditText) {
        val c = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, h, m ->
                editText.setText(String.format("%02d:%02d", h, m))
            },
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
        ).show()
    }

    private fun guardarSolicitud() {
        val uid = auth.currentUser?.uid ?: return

        val datos = mapOf(
            "fechaInicio" to etFechaInicio.text.toString(),
            "fechaFin" to etFechaFin.text.toString(),
            "horaInicio" to etHoraInicio.text.toString(),
            "horaFin" to etHoraFin.text.toString(),
            "ubicacion" to etUbicacion.text.toString(),
            "descripcionSolicitud" to etServicio.text.toString(),
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val dialog = mostrarProgressDialog("Guardando solicitud...")
        db.collection("solicitudes").add(datos)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(this, "Solicitud guardada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
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
