package com.app.pawcare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1001
    private val TAG = "RegistroActivity"

    // Datos temporales para esperar la ubicación
    private lateinit var pendingUsername: String
    private lateinit var pendingEmail: String
    private lateinit var pendingPassword: String
    private var pendingIsCaregiver: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = Firebase.auth
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val switchUser = findViewById<Switch>(R.id.switchUser)
        val switchCaregiver = findViewById<Switch>(R.id.switchCaregiver)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val isUser = switchUser.isChecked
            val isCaregiver = switchCaregiver.isChecked

            if (!validarDatos(username, email, password, isUser, isCaregiver)) return@setOnClickListener

            // Guardar datos temporalmente mientras se procesa la ubicación
            pendingUsername = username
            pendingEmail = email
            pendingPassword = password
            pendingIsCaregiver = isCaregiver

            // Paso 1: Intentar obtener ubicación
            obtenerUbicacionParaRegistro()
        }

        // Lógica de switches
        switchUser.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchCaregiver.isChecked = false
        }
        switchCaregiver.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) switchUser.isChecked = false
        }
    }

    private fun validarDatos(username: String, email: String, password: String, isUser: Boolean, isCaregiver: Boolean): Boolean {
        if (username.isEmpty()) {
            findViewById<EditText>(R.id.etUsername).error = "Nombre requerido"
            return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            findViewById<EditText>(R.id.etEmail).error = "Correo inválido"
            return false
        }
        if (password.length < 6) {
            findViewById<EditText>(R.id.etPassword).error = "Mínimo 6 caracteres"
            return false
        }
        if (!isUser && !isCaregiver) {
            Toast.makeText(this, "Selecciona un rol", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun obtenerUbicacionParaRegistro() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido: obtener ubicación
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    val ciudad = if (location != null) obtenerNombreCiudad(location) else "Ubicación no detectada"
                    iniciarRegistroConUbicacion(ciudad)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al obtener ubicación", e)
                    iniciarRegistroConUbicacion("Ubicación no disponible")
                }
        } else {
            // Pedir permisos (el registro continuará en onRequestPermissionsResult)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionParaRegistro()
            } else {
                // Permiso denegado: registrar con valor por defecto
                iniciarRegistroConUbicacion("Ubicación no especificada")
            }
        }
    }

    private fun iniciarRegistroConUbicacion(ciudad: String) {
        val progressDialog = mostrarProgressDialog("Registrando usuario...")

        auth.createUserWithEmailAndPassword(pendingEmail, pendingPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = hashMapOf(
                        "userId" to user?.uid,
                        "username" to pendingUsername,
                        "email" to pendingEmail,
                        "role" to if (pendingIsCaregiver) "caregiver" else "user",
                        "profileImage" to "",
                        "location" to ciudad,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    user?.let {
                        db.collection("users")
                            .document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                redirigirSegunRol(pendingIsCaregiver)
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error en registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun obtenerNombreCiudad(location: Location): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.let {
                "${it.locality ?: "Ubicación desconocida"}${if (it.countryName != null) ", ${it.countryName}" else ""}"
            } ?: "Ubicación no disponible"
        } catch (e: Exception) {
            Log.e(TAG, "Error en Geocoder", e)
            "Ubicación no disponible"
        }
    }

    private fun redirigirSegunRol(isCaregiver: Boolean) {
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

        val intent = if (isCaregiver) {
            Intent(this, CuidadorActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun mostrarProgressDialog(mensaje: String): AlertDialog {
        val view = layoutInflater.inflate(R.layout.dialog_progress, null)
        val tvMensaje = view.findViewById<TextView>(R.id.tv_progress_message)
        tvMensaje.text = mensaje

        return AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()
            .also { it.show() }
    }
}
